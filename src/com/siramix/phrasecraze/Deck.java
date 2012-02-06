/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.siramix.phrasecraze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.siramix.phrasecraze.Consts.PurchaseState;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Deck represents the stack of all cards in the game. We interact with a
 * SQLite database that stores the cards and intelligently caches them at the
 * beginning of the game and replenishes the cache at the beginning of each
 * turn. Each instance of PhraseCraze contains the same database; however the
 * sorted order is determined using a pseudo-random variable. This variable is
 * determined on the first run of the game using the Java Random class and has a
 * something to do with cpuid, system time, etc. The variable, called mSeed, is
 * used to shuffle the deck with a generating function such that the sort can be
 * repeated on every subsequent load of the game. Thus, we can store only on the
 * seed and an offset to preserve the sorted order of the players' deck. We also
 * save mCache to prevent any unnecessary card loss between sessions.
 * 
 * @author Siramix Labs
 */
public class Deck {

  private static final String TAG = "Deck";

  private static final String DATABASE_NAME = "phrasecraze";
  private static final String PHRASE_TABLE_NAME = "phrases";
  private static final String CACHE_TABLE_NAME = "cache";
  private static final String PACK_TABLE_NAME = "packs";
  private static final int DATABASE_VERSION = 1;
  private static final int PHRASECACHE_SIZE = 200;
  
  private static final String[] PHRASE_COLUMNS = { "id", "phrase", "difficulty", 
                                                   "playdate", "pack_id" };
  private static final String[] PACK_COLUMNS = { "id", "packname", "version" };
  private static final String[] CACHE_COLUMNS = { "id", "val" };
  
  private static final String PHRASE_TABLE_CREATE = "CREATE TABLE "
      + PHRASE_TABLE_NAME + "( " + 
          PHRASE_COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " +  
          PHRASE_COLUMNS[1] + " TEXT, " + 
          PHRASE_COLUMNS[2] + " INTEGER, " +
          PHRASE_COLUMNS[3] + " INTEGER, " +
          PHRASE_COLUMNS[4] + " INTEGER, " + 
          "FOREIGN KEY(" + PACK_COLUMNS[0] + ") REFERENCES " + PACK_TABLE_NAME + "(" + PACK_COLUMNS[0] + ") );";
  private static final String CACHE_TABLE_CREATE = "CREATE TABLE "
      + CACHE_TABLE_NAME + "( " + 
          CACHE_COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          CACHE_COLUMNS[1] + " TEXT );";
  private static final String PACK_TABLE_CREATE = "CREATE TABLE " 
      + PACK_TABLE_NAME + "( " + 
          PACK_COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          PACK_COLUMNS[1] + " TEXT, " + 
          PACK_COLUMNS[2] + " INTEGER );";
  
  // Take the top 1/DIVISOR phrases from a pack as possible cards for the phraseCache
  //TODO This should be weighted
  private static final int PACK_DIVISOR = 25; 
  
  // This is the sum of all cards in selected packs
  private int mPackTotalCards;
  
  // This will get set during Game Setup, it's the ideal number of cards for a single game
  // Potentially this will be dependent on num rounds selected
  private int mCacheSize;
  
  // After taking the top 1/DIVSOR phrases from a pack, throw back a percentage of them 
  private static final int THROW_BACK_PERCENTAGE = 20;
  
  private LinkedList<Card> mPhraseCache;    
  private Context mContext;

  private DeckOpenHelper mDatabaseOpenHelper;

  /**
   * Constructor
   * 
   * @param context
   *          The Context within which to work, used to create the DB
   */
  public Deck(Context context) {
    mContext = context;
    mDatabaseOpenHelper = new DeckOpenHelper(context);
    mPhraseCache = new LinkedList<Card>();
    mDatabaseOpenHelper.close();
  }

  /**
   * Prepare for a game by caching the phrases necessary for the entire game.  
   * Ideally we should only do this in between games. 
   */
  public void prepareForGame() {
    mDatabaseOpenHelper = new DeckOpenHelper(mContext);
    
    // Use the preferences to pull the chosen decks as these will
    // need to be set anyways to save user's last game's settings
    
    //TODO FIGURE OUT WEIGHTING
    
    
    //TODO STUB
    mPackTotalCards = 125;
    mCacheSize = 100;
    
    // For all packs being played get the phrases we want  
    mPhraseCache.addAll(mDatabaseOpenHelper.pullFromPack("starter", mCacheSize, mPackTotalCards));
    
    // Put all the phrases together
    
    // Fill the cache from the database and shuffle
    //mPhraseCache.addAll(mDatabaseOpenHelper.getPhrases(ids));
    Collections.shuffle(mPhraseCache);
        
    mDatabaseOpenHelper.close();
  }
  
  //TODO LOOK THIS OVER, got deleted and added back...WHY?
  /**
   * Get the card from the top of the cache
   * 
   * @return a card reference
   */
  public Card getPhrase() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getCard()");
    }
    if (mPhraseCache.isEmpty()) {
      this.prepareForGame();
      return mPhraseCache.removeFirst();
    } else {
      return mPhraseCache.removeFirst();
    }
  }
  
  /**
   * Sets the value for how many cards will be played for the night.  Should
   * be set before a game begins and after packs are chosen.  This may be
   * proportional to all the cards selected.  We should also validate that
   * players chose a 'good' amount, i.e. not playing with just 200 cards.
   * 
   * @param cacheSize The number of cards to store for a game's worth of cards 
   * @return
   */
  public void setCacheSize(int cacheSize) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setPlaySize()");
    }
    this.mCacheSize = cacheSize;
  }

  /**
   * Add a pack to the sytem
   * @return an integer indicating the number of packs processed
   */
  public synchronized int updatePurchase(String orderId, String productId,
          PurchaseState purchaseState, long purchaseTime, String developerPayload) {
    if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "updatePurchase()");
      }
    Log.d(TAG, "STUB");
    Log.d(TAG, orderId);
    Log.d(TAG, productId);
    Log.d(TAG, purchaseState.name());
    Log.d(TAG, Long.toString(purchaseTime));
    Log.d(TAG, Long.toString(purchaseTime));
    Log.d(TAG, developerPayload);
    return 0;
  }

  /**
   * This class creates/opens the database and provides helper functions for
   * batch CRUD operations
   */
  public static class DeckOpenHelper extends SQLiteOpenHelper {

    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;

    /**
     * Default Constructor from superclass
     * 
     * @param context
     */
    DeckOpenHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      mHelperContext = context;
    }

    /**
     * Create the tables and populate from the XML file
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(PACK_TABLE_CREATE);
      db.execSQL(PHRASE_TABLE_CREATE);
      db.execSQL(CACHE_TABLE_CREATE);
      digestPack(db, "starter");
    }

    /**
     * Count the phrases in the deck quickly
     * 
     * @return the number of phrases in the deck
     */
    public int countPhrases() {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "countPhrases()");
      }
      mDatabase = getWritableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, PHRASE_TABLE_NAME);
      return ret;
    }

    /**
     * Count the number of cache entries (This should NEVER be > 1)
     * 
     * @return the number of cache entries
     */
    public int countCaches() {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "countCaches()");
      }
      mDatabase = getWritableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, CACHE_TABLE_NAME);
      return ret;
    }
    
    /**
     * Count the number of packs which will likely be needed for setting up views
     * 
     * @return the number of packs
     */
    public int countPacks() {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "countPacks()");
      }
      mDatabase = getWritableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, PHRASE_TABLE_NAME);
      return ret;
    }

    /**
     * Load the words from the XML file using only one SQLite database.  Packs
     * are broken out into separate XML files to allow for in-app purchasing.
     * 
     * @param db from the installing context
     * @param packFileName the name of the file to digest
     */
    private void digestPack(SQLiteDatabase db, String packFileName) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Digesting pack " + packFileName + "...");
      }

      mDatabase = db;

      // Dynamically retrieve the xml resource Id for the pack name
      int resId = mHelperContext.getResources().
          getIdentifier(packFileName, "raw", "com.siramix.phrasecraze");
      BufferedReader packJSON = new BufferedReader(new InputStreamReader(
          mHelperContext.getResources().openRawResource(resId)));

      try {
        mDatabase.beginTransaction();
        int packId = (int) insertPack(packFileName, mDatabase);
        CardJSONIterator cardItr = PackParser.parseCards(packJSON);
        Card curCard = null;
        while(cardItr.hasNext()) {
          curCard = cardItr.next();
          insertPhrase(curCard.getTitle(), 1, packId, mDatabase);
        }
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }

      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "DONE loading words.");
      }
    }

    /**
     * Insert a phrase to the deck (DB)
     * 
     * @return rowId or -1 if failed
     */
    public static long insertPhrase(String phrase, int difficulty, int packId, SQLiteDatabase db) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "insertPhrase()");
      }
      ContentValues initialValues = new ContentValues();
      initialValues.put("phrase", phrase);
      initialValues.put("difficulty", difficulty);
      initialValues.put("playdate", 0);
      initialValues.put("pack_id", packId);
      return db.insert(PHRASE_TABLE_NAME, null, initialValues);
    }
    
    /**
     * Insert a new pack into the Pack table of a given database.
     * @param packname The name of the pack to insert
     * @param db The db in which to insert the new pack
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long insertPack(String packname, SQLiteDatabase db) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "addPack()");
      }
      ContentValues packValues = new ContentValues();
      packValues.put("packname", packname);
      return db.insert(PACK_TABLE_NAME, null, packValues);
    }   

    /**
     * Get the phrases corresponding to a comma-separated list of indices
     * 
     * @param args indices separated by commas
     * @param packName Exact match of the name of the pack in the database
     * @return a reference to a linked list of cards corresponding to the ids
     */
    public LinkedList<Card> getPhrases(String args, String packIds) {
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "getPhrases()");
      }
      // TODO: Refactor this once it's working so we can make sure it is efficient 
      // and syntactically elegant (with ?s) 
      Cursor res = mDatabase.query(PHRASE_TABLE_NAME, PHRASE_COLUMNS, "id IN ("
          + args + ") and pack_id IN ( " + packIds + ")", null, null, null, null);
      res.moveToFirst();
      LinkedList<Card> ret = new LinkedList<Card>();
      while (!res.isAfterLast()) {
        if (PhraseCrazeApplication.DEBUG) {
          Log.d(TAG, res.getString(1));
        }
        ret.add(new Card(res.getInt(0), res.getString(1)));
        res.moveToNext();
      }
      res.close();
      return ret;
    }
    
    public int getPackId(String packname) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "getPackId(" + packname + ")");
      }
      mDatabase = getWritableDatabase();
      
      // TODO: Question for code review.  Better to do a join or two separate 
      // Get our pack ID 
      Cursor res = mDatabase.query(PACK_TABLE_NAME, PACK_COLUMNS, "packname = '" + packname +"'", null, null, null, null);
      
      res.moveToFirst();
      int packid = res.getInt(0);      
      
      res.close();
      return packid;
    }
    /**
     * Update playdate for all passed in phrase ids to current time
     * @param ids
     *          comma delimited set of phrase ids to incrment, ex. "1, 2, 4, 10"
     * @return
     */
    public void updatePlaydate(String ids) {
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "updatePlaydate()");
      }
      
      //TODO for code review:  For some reason the database.update command was interpreting the
      // playcount+1 as a string and inserting that string instead of actually incrementing
      // If its all the same to everyone, I went ahead and hardcoded this query to work around this
      // There is also a known issue with this...if a card is seen twice in a single round
      // it will only get updated once.  To me the issue of seeing a card twice is the real issue
      // and it's not worth fixing the count for that severe case.
//      ContentValues newValues = new ContentValues();
//      newValues.put("playcount", "playcount+1");
//      mDatabase.update(PHRASE_TABLE_NAME, newValues, "id in (" + args + ")", null);
      mDatabase.execSQL("UPDATE " + PHRASE_TABLE_NAME  
                      + " SET playdate = datetime('now')"
                      + " WHERE id in(" + ids + ");"); 
    }
    
    /**
     * Saves the cache in the database
     * 
     * @param cache
     *          Linked list of cards to insert (by DB index)
     */
    public void saveCache(LinkedList<Card> cache) {
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "saveCache()");
      }
      String cacheString = "";
      for (Iterator<Card> itr = cache.iterator(); itr.hasNext();) {
        cacheString += itr.next().getId();
        if (itr.hasNext()) {
          cacheString += ",";
        }
      }
      
      ContentValues values = new ContentValues();
      values.put("id", 0);
      values.put("val", cacheString);
      if (this.countCaches() >= 1) {
        mDatabase.update(CACHE_TABLE_NAME, values, "", null);
      } else {
        mDatabase.insert(CACHE_TABLE_NAME, null, values);
      }
    }

    /**
     * Load a cache from the database into phrases in memory
     * @return the linked list of phrases loaded into memory
     */
    public LinkedList<Card> loadCache() {      
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "loadCache()");
      }
      mDatabase = getWritableDatabase();
      Cursor res = mDatabase.query(CACHE_TABLE_NAME, CACHE_COLUMNS, "id in (0)", null,
          null, null, null);
      LinkedList<Card> ret;
      if (res.getCount() == 0) {
        ret = new LinkedList<Card>();
      } else {
        res.moveToFirst();
        // TODO MAYBE THIS WHOLE METHOD GOES?
        ret = getPhrases(res.getString(1), "1");
      }
      res.close();
      return ret;
    }
    
    public LinkedList<Card> pullFromPack(String packname, int CACHE_SIZE, int TOTAL_SELECTED) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "pullFromPack(" + packname + ")");        
      }
      mDatabase = getWritableDatabase();      
      
      LinkedList<Card> ret = new LinkedList<Card>();      
      int packid = getPackId(packname);     

      // Get the phrases from pack, sorted by playdate, and no need to get more than the CACHE_SIZE      
      Cursor res = mDatabase.query(PHRASE_TABLE_NAME, PHRASE_COLUMNS, "pack_id = " + packid, 
                                  null, null, null, "playdate asc", Integer.toString(CACHE_SIZE));
      res.moveToFirst();
      
      // The number of cards to return from any given pack will use the following formula:
      // (WEIGHT OF PACK) * CACHE_SIZE + SURPLUS --> Then we randomly take out X cards where X = SURPLUS
      int packsize = res.getCount();
      float weight = (float) packsize / (float) TOTAL_SELECTED;      
      int targetnum = (int) Math.ceil(CACHE_SIZE * weight);
      int surplusnum = (int) Math.ceil( (float) targetnum * (Deck.THROW_BACK_PERCENTAGE / 100));
      int workingnum = targetnum + surplusnum;
      
      Log.d(TAG, "** packsize: " + packsize);
      Log.d(TAG, "** weight: " + weight);
      Log.d(TAG, "** targetnum: " + targetnum);
      Log.d(TAG, "** surplusnum: " + surplusnum);
      Log.d(TAG, "** workingnum: " + workingnum);
      
      Log.d(TAG, "** ADDING PHRASES");
      // Add the first workingnum cards, throwing out THROW_BACK_PERCENTAGE of the cards seen
      while (!res.isAfterLast() && res.getPosition() < workingnum) {
        if (PhraseCrazeApplication.DEBUG) {
          Log.d(TAG, "adding: " + res.getString(1));
        }        
        ret.add(new Card(res.getInt(0), res.getString(1)));
        res.moveToNext();
      }
      Log.d(TAG, "**" + ret.size() + " phrases added.");
      
      // Throw out x surplus cards at random
      Random r = new Random();
      int removeCount = 0;
      int index = 0;
      Log.d(TAG, "** REMOVING PHRASES");
      while (removeCount < surplusnum) {
        index = r.nextInt(ret.size()-1);
        Log.d(TAG, "**removing: " + ret.get(index));
        ret.remove(index);
        removeCount++;
      }
      Log.d(TAG, "**" + removeCount + " phrases removed.");
      
      res.close();
      return ret;
    }

    //TODO Let's reconsider if this is the best thing to do after we figure out 
    // how marketplace updates will affect each phone's database
    /**
     * For now, onUpgrade destroys the old database and runs create again.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS phrases;");
      db.execSQL("DROP TABLE IF EXISTS packs;");
      db.execSQL("DROP TABLE IF EXISTS cache;");
      onCreate(db);
    }

    /**
     * Make sure we close the database when we close the helper
     */
    @Override
    public void close() {
      super.close();
      if (mDatabase != null) {
        mDatabase.close();
      }
    }
  }

}
