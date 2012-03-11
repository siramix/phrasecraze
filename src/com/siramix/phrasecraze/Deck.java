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
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.siramix.phrasecraze.Consts.PurchaseState;

import android.R.string;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;

/**
 * The Deck represents the phrases that will be used in any single game
 * of PhraseCraze.  The Deck consists of two core pieces: a list of cards
 * in play, and a list of cards cached for future play in the same game.
 * Also included in this class is a SQLLite helper class that will support
 * creation and consumption of phrases at the database level.
 * 
 * @author Siramix Labs
 */
public class Deck {

  private static final String TAG = "Deck";

  private static final String DATABASE_NAME = "phrasecraze";
  private static final int DATABASE_VERSION = 1;
  protected static final int BACK_CACHE_MAXSIZE = 10;
  protected static final int FRONT_CACHE_MAXSIZE = 5;
  
  private static final int PACK_CURRENT = -1;
  private static final int PACK_NOT_PRESENT = -2;
  
  // TODO We need to look at ALL OF THE QUERIES in this class.

  // This is the sum of all cards in selected packs
  private int mTotalSelectedCards;
  
  // After taking the top 1/DIVSOR phrases from a pack, throw back a percentage of them 
  private static final int THROW_BACK_PERCENTAGE = 0;
  
  // A list of back cards used for refreshing the deck.  Will be filled after it reaches 0.
  private LinkedList<Card> mBackCache;
  
  // Front Cache will be topped off every turn
  private LinkedList<Card> mFrontCache;
  
  // Should be wiped every time the mbackCache is cleared
  private LinkedList<Card> mSeenCards;
  
  // List of all packs selected for the Deck
  private LinkedList<Pack> mSelectedPacks;
  
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
    mBackCache = new LinkedList<Card>();
    mFrontCache = new LinkedList<Card>();
    mSeenCards = new LinkedList<Card>();
    mSelectedPacks = new LinkedList<Pack>();
    // Set all our deck properties to use for later calculations
    setDeckProperties();
  }

  /**
   * Get a card from the top of the Front Cache queue.  Once
   * this reaches the bottom of the deck, we should top off the Deck which
   * will in turn trigger a pull from packs to refill the cache.
   * @return a card reference
   */
  public Card dealPhrase() {
    Card ret;
    if (mFrontCache.isEmpty()) {
      this.topOffFrontCache();
    }
    ret = mFrontCache.removeFirst();
    Log.i(TAG, " Dealing ::" + ret.getTitle() + ":: Difficulty- " + ret.getDifficulty() 
          + " Pack: ???");
    mSeenCards.add(ret);
    return ret;
  }
  
  /**
   * If there aren't enough cards in the back cache to fill the 
   * front-facing cache at least once, fill up both caches.  This
   * should be called during downtime since it could be a costly
   * database pull.
   */
  public void fillCachesIfLow() {
    Log.d(TAG, "fillCachesIfLow()");
    topOffFrontCache();
    if (mBackCache.size() < FRONT_CACHE_MAXSIZE) {
      Log.d(TAG, "...Back Cache size was low (" + mBackCache.size() + "), filling...");
      mBackCache.clear();
      fillBackCache();
      Log.d(TAG, "...filled. Back Cache size is now " + mBackCache.size());
    }
  }

  private void topOffFrontCache() {
    Log.d(TAG, "topOffFrontCache()");
    int lack = FRONT_CACHE_MAXSIZE - mFrontCache.size();
    Log.d(TAG, "*** FRONT CACHE MAX SIZE: " + FRONT_CACHE_MAXSIZE);
    Log.d(TAG, "*** Front Cache Size: " + mFrontCache.size());
    Log.d(TAG, "*** Lack: " + String.valueOf(lack));    
    Log.d(TAG, "*** Current Back Cache Size: " + String.valueOf(mBackCache.size()));
    
    for (int i=0; i<lack; ++i) {
      mFrontCache.add(popBackCache());
    }
    printDeck();
  }

  /**
   * Get the card from the top of the cache
   * 
   * @return a card reference
   */
  private Card popBackCache() {
    Card ret;
    // If we reach this scenario it means a lot of cards were looked at during a turn
    // Otherwise it should be filled by a GameManager.maintainDeck call
    if (mBackCache.isEmpty()) {
      mDatabaseOpenHelper.updatePlaydate(mSeenCards);
      mSeenCards.clear();
      this.fillBackCache();
    }
    ret = mBackCache.removeFirst();
    Log.d(TAG, " Popped " + ret.getTitle() + " from cache.");
    return ret;
  }
  
  /**
   * Take a Pack object and pull in cards from the server into the database. 
   * @param pack
   * @throws IOException
   * @throws URISyntaxException
   */
  public synchronized void digestPack(Pack pack) throws RuntimeException {
    try {
      mDatabaseOpenHelper.digestPackFromServer(pack);
    } catch (IOException e) {
      RuntimeException userException = new RuntimeException(e);
      throw userException;
    } catch (URISyntaxException e) {
      RuntimeException userException = new RuntimeException(e);
      throw userException;
    }
  }

  private void setDeckProperties() {

  }
  
  /**
   * Prepare for a game by caching the phrases necessary for the entire game.  
   * Ideally we should only do this in between games. 
   */
  private void fillBackCache() {
    Log.d(TAG, "fillBackCache()");
    printDeck();
    Log.i(TAG, "filling back cache...");
    mDatabaseOpenHelper = new DeckOpenHelper(mContext);
    
    // 1. Use the preferences to find the chosen packs to pull from 
    setSelectedPacks();
    
    // 2. Count how many phrases are selected and determine weights
    Log.d(TAG, "2. Weight Calculations:");
    mTotalSelectedCards = mDatabaseOpenHelper.countEligiblePhrases(mSelectedPacks);
    int numSelectedPacks = mSelectedPacks.size();
    float[] remainderWeights = new float[numSelectedPacks];
    int[] targetNumForPacks = new int[numSelectedPacks];
    int targetNumSum = 0;
    int lack = 0;
    
    // 2.a. Calculate targetNumForPacks
    for (int i=0; i<numSelectedPacks; ++i) {
      int packSize = mDatabaseOpenHelper.countEligiblePhrases(mSelectedPacks.get(i));
      remainderWeights[i] = (float) packSize / (float) mTotalSelectedCards;
      Log.d(TAG, "** Total Selected Cards: " + mTotalSelectedCards);
      Log.d(TAG, "** pack: " + mSelectedPacks.get(0).getName());
      Log.d(TAG, "** pack size: " + packSize);
      Log.d(TAG, "** pack weight: " + remainderWeights[i]);
      lack = Deck.BACK_CACHE_MAXSIZE - mBackCache.size();
      float portion = lack * remainderWeights[i];
      targetNumForPacks[i] = (int) Math.floor(portion);
      targetNumSum += targetNumForPacks[i];
      remainderWeights[i] = portion - targetNumForPacks[i];
      Log.d(TAG, "** remainder weight: " + remainderWeights[i]);
      Log.d(TAG, "** target num: " + targetNumForPacks[i]);
    }
    
    // 2.b. Allocate remainder based on the "cut off floor"
    int remainder = lack - targetNumSum;
    Log.d(TAG, "Assigning remainder of " + remainder + " cards.");
    Random randomizer = new Random();
    
    // Build our array for determining odds
    // ex:
    //  0: 0.3 (30%)
    //  1: 0.3-0.7 (40%)
    //  2: 0.7-1.0 (30%)
    final int RAND_PRECISION = 1000;
    int[] odds = new int[numSelectedPacks];
    odds[0] = (int) (remainderWeights[0]*RAND_PRECISION);
    Log.d(TAG, "..first odds range: 0-" + odds[0]);
    for (int i=1; i<odds.length; ++i) {
      odds[i] = (int) ((remainderWeights[i-1] + remainderWeights[i])*RAND_PRECISION);
      Log.d(TAG, "..next odds range at: " + odds[i-1] + "-" + odds[i]);
    }
    int rand = 0;
    
    // For each remaining card, randomly choose a pack to 
    // pull from, weighting based on the floor weight
    for (int i=0; i<remainder; ++i) {
      // Use a precision of 3 decimals when selecting a pack
      rand = randomizer.nextInt(remainder*RAND_PRECISION);
      Log.d(TAG, "** random number is: " + rand);
      
      // iterate through pack remainderWeights to see which pack rand landed on
      if ( rand <= odds[0]) {
        ++targetNumForPacks[0];
        Log.d(TAG, "...assigned a remainder to: " + mSelectedPacks.get(0));
      }
      else {  
        for (int j=1; j<remainderWeights.length; ++j) {
          int low = odds[j-1];
          int high = odds[j];
          if (rand > low && rand <= high)
          {
            ++targetNumForPacks[j];
            Log.d(TAG, "...assigned a remainder to: " + mSelectedPacks.get(j));
          }
        }
      }
    }
    
    // 3. Fill our cache up with cards from all selected packs (using sorting algorithm)
    Log.d(TAG, "3. Pull Calculations: ");
    for (int i=0; i<numSelectedPacks; ++i) {
      mBackCache.addAll(mDatabaseOpenHelper.pullFromPack(mSelectedPacks.get(i), mFrontCache, 
                                                    targetNumForPacks[i]));
    }
    
    // 4. Now shuffle
    Collections.shuffle(mBackCache);
    
    mDatabaseOpenHelper.close();
    Log.i(TAG, "filled.");
    printDeck();
  }
  
  private void setSelectedPacks() {
    SharedPreferences packPrefs = mContext.getSharedPreferences(
            Consts.PREF_PACK_SELECTIONS, Context.MODE_PRIVATE);
    Map<String, ?> packSelections = new HashMap<String, Boolean>();
    packSelections = packPrefs.getAll();
    
    // TODO starter.json should be selected through the front end, not hard-coded here
    //selectedPacks.add("starter");
    //selectedPacks.add("allphrases");
    Pack pack1 = mDatabaseOpenHelper.getPackFromDB(String.valueOf(R.raw.pack1));
    Pack pack2 = mDatabaseOpenHelper.getPackFromDB(String.valueOf(R.raw.pack2));
    mSelectedPacks.add(pack1);
    mSelectedPacks.add(pack2);
    for (String packId : packSelections.keySet()) {
      if (packPrefs.getBoolean(packId, false) == true) {
        mSelectedPacks.add(mDatabaseOpenHelper.getPackFromDB(packId));
      }
    }
  }
  

  private void calculateWeightings() {
    
  }
  
  /**
   * Debugging function.  Can be removed later.
   */
  public void printDeck() {
    Log.d(TAG, "printDeck...");
    Log.d(TAG, "========================");
    Log.d(TAG, "FRONT CACHE: ");
    Log.d(TAG, "Front Cache Size is " + mFrontCache.size());
    for (int i=0; i<mFrontCache.size(); ++i) {
      Log.d(TAG, "..." + mFrontCache.get(i).getTitle());
    }
    Log.d(TAG, "END FRONT CACHE");
    Log.d(TAG, "------------------------");
    Log.d(TAG, "BACK CACHE: ");
    Log.d(TAG, "Back Cache Size is " + mBackCache.size());
    for (int i=0; i<mBackCache.size(); ++i) {
      Log.d(TAG, "..." + mBackCache.get(i).getTitle());
    }
    Log.d(TAG, "END BACK CACHE");
    Log.d(TAG, "========================");
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
      db.execSQL(PackColumns.TABLE_CREATE);
      db.execSQL(PhraseColumns.TABLE_CREATE);
//      digestPackFromResource(db, "starter", R.raw.starter);
//      digestPackFromResource(db, "allphrases", R.raw.allphrases);

      digestPackFromResource(db, "pack1", R.raw.pack1);
      digestPackFromResource(db, "pack2", R.raw.pack2);
    }

    /**
     * Count all phrases in the deck quickly
     * 
     * @return the number of phrases in the deck
     */
    public int countAllPhrases() {
      Log.d(TAG, "countPhrases()");
      mDatabase = getReadableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, PhraseColumns.TABLE_NAME);
      return ret;
    }
    
    /**
     * Returns an integer count of all phrases associated with the passed in pack names
     * @param packFileNames The filenames of all packs to be counted
     * @return -1 if no phrases found, otherwise the number of phrases found
     */
    public int countEligiblePhrases(LinkedList<Pack> packs) {
      Log.d(TAG, "countEligiblePhrases(LinkedList<String>)");
      String[] args = new String[2];

      args[0] = buildPackIdString(packs);
      args[1] = buildDifficultyString();
      
      mDatabase = getWritableDatabase();
      
      //TODO WHy the HELL didn't that work?
//      Cursor countQuery = mDatabase.query(PhraseColumns.TABLE_NAME, PhraseColumns.COLUMNS,
//          PhraseColumns.PACK_ID + " IN (?)", packIds, null, null, null);
      
      Cursor countQuery = mDatabase.rawQuery("SELECT * " + 
          " FROM " + PhraseColumns.TABLE_NAME + 
          " WHERE " + PhraseColumns.PACK_ID + " IN (" + args[0] + ")" + 
          "   AND " + PhraseColumns.DIFFICULTY + " IN (" + args[1] + ")", null);
      int count = countQuery.getCount();

      return count;
    }

    /**
     * Returns an integer count of all phrases associated with the passed in pack names
     * @param packFileNames The filenames of all packs to be counted
     * @return -1 if no phrases found, otherwise the number of phrases found
     */
    public int countEligiblePhrases(Pack pack) {
      Log.d(TAG, "countEligiblePhrases(pack");
      String[] args = new String[2];

      args[0] = String.valueOf(pack.getId());
      args[1] = buildDifficultyString();
      
      mDatabase = getWritableDatabase();
      
      //TODO WHy the HELL didn't that work?
//      Cursor countQuery = mDatabase.query(PhraseColumns.TABLE_NAME, PhraseColumns.COLUMNS,
//          PhraseColumns.PACK_ID + " IN (?)", packIds, null, null, null);
      
      Cursor countQuery = mDatabase.rawQuery("SELECT * " + 
          " FROM " + PhraseColumns.TABLE_NAME + 
          " WHERE " + PhraseColumns.PACK_ID + " IN (" + args[0] + ")" + 
          "   AND " + PhraseColumns.DIFFICULTY + " IN (" + args[1] + ")", null);
      int count = countQuery.getCount();
      
      return count;
    }
    
    /**
     * Count the number of packs which will likely be needed for setting up views
     * 
     * @return the number of packs
     */
    public int countPacks() {
      Log.d(TAG, "countPacks()");

      mDatabase = getReadableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, PackColumns.TABLE_NAME);
      return ret;
    }

    /**
     * Return a Pack instantiated using the entry in the Pack database.
     * @param packId of the pack you wish to instantiate
     * @return
     */
    public Pack getPackFromDB(String packId) {
      Log.d(TAG, "getPackFromDB(" + String.valueOf(packId) + ")");
      mDatabase = getReadableDatabase();
      String[] id = new String[] {packId};
      
      Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME,PackColumns.COLUMNS, 
          PackColumns._ID + "=?", id, null, null, null);
      
      Pack pack = null;
      if (packQuery.moveToFirst()) {
        pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
                        packQuery.getString(3), null, packQuery.getInt(4), packQuery.getInt(5));
      }
      packQuery.close();
      return pack;
    }

    /**
     * Load the words from the JSON file using only one SQLite database. This
     * function loads the words from a json file that is stored as a resource in the project
     * 
     * @param db from the installing context
     * @param packName the name of the file to digest
     * @param resId the resource of the pack file to digest
     */
    private void digestPackFromResource(SQLiteDatabase db, String packName, int resId) {
      Log.d(TAG, "Digesting pack from resource " + String.valueOf(resId));

      BufferedReader packJSON = new BufferedReader(new InputStreamReader(
          mHelperContext.getResources().openRawResource(resId)));
      StringBuilder packBuilder = new StringBuilder();
      String line = null;
      try {
        while((line = packJSON.readLine()) != null) {
          packBuilder.append(line).append("\n");
        }
      } catch (IOException e) {
        Log.e(TAG,"Problem Reading pack from Resource.");
        e.printStackTrace();
      }
      CardJSONIterator cardItr = PackParser.parseCards(packBuilder);
      
      Pack insertPack = new Pack(resId, packName, "RESOURCE PACK", "From Resource", null, 0, 1000);
      digestPackInternal(db, insertPack, cardItr);

      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "DONE loading words.");
      }
    }

    public void digestPackFromServer(Pack pack) throws IOException, URISyntaxException {
      mDatabase = getWritableDatabase();
      // Don't add a pack if it's already there
      int packId = packInstalled(pack.getName(), pack.getVersion(), mDatabase);
      if (packId == PACK_CURRENT) {
        return;
      }
      if(packId != PACK_NOT_PRESENT) { 
        clearPack(packId, mDatabase);
      }
      CardJSONIterator cardItr = PackClient.getInstance().getCardsForPack(pack);
      digestPackInternal(mDatabase, pack, cardItr);
    }

    /**
     * Replaces or inserts a new row into the Packs table and then replaces or inserts
     * rows into the Phrases table for each phrase in that pack.
     * 
     * @param db
     * @param packName
     * @param packVersion
     * @param cardItr
     */
    private static void digestPackInternal(SQLiteDatabase db, Pack pack, CardJSONIterator cardItr) {
      Log.d(TAG, "digestPackInternal: " + pack.getName() + "v" + String.valueOf(pack.getVersion()));
 
      // Add the pack and all cards in a single transaction.
      try {
        db.beginTransaction();
        upsertPack(pack, db);
        Card curCard = null;
        while(cardItr.hasNext()) {
          curCard = cardItr.next();
          upsertPhrase(curCard, pack.getId(), db);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }

    /**
     * Replaces existing phrase if it exists, otherwise inserts the phrase in the Phrases table.
     * 
     * @return rowId or -1 if failed
     */
    public static long upsertPhrase(Card phrase, int packId, SQLiteDatabase db) {
      Log.d(TAG, "upsertPhrase(" + phrase + ")");
      
      ContentValues initialValues = new ContentValues();
      initialValues.put(PhraseColumns._ID, phrase.getId());
      initialValues.put(PhraseColumns.PHRASE, phrase.getTitle());
      initialValues.put(PhraseColumns.DIFFICULTY, phrase.getDifficulty());
      initialValues.put(PhraseColumns.PLAY_DATE, 0);
      initialValues.put(PhraseColumns.PACK_ID, packId);
      return db.replace(PhraseColumns.TABLE_NAME, null, initialValues);
    }
    
    /**
     * Either insert a new pack into the Pack table of a given database or replace
     * one that e 
     * @param pack The pack object to insert into db
     * @param db The db in which to insert the new pack
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long upsertPack(Pack pack, SQLiteDatabase db) {
      Log.d(TAG, "insertPack()");
      
      ContentValues packValues = new ContentValues();
      packValues.put(PackColumns._ID, pack.getId());
      packValues.put(PackColumns.NAME, pack.getName());
      packValues.put(PackColumns.PATH, pack.getPath());
      packValues.put(PackColumns.VERSION, pack.getVersion());
      return db.replace(PackColumns.TABLE_NAME, null, packValues);
    }
    
    /**
     * Queries the Packs table and returns the packId if the pack requires updating, 
     * otherwise returns either PACK_CURRENT or PACK_NOT_PRESENT.
     * @param packName 
     * @param packVersion Latest version of the pack
     * @param db
     * @return
     */
    public static int packInstalled(String packName, int packVersion, SQLiteDatabase db) {
      String[] packNames = {packName};
      Cursor res = db.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS,
          PackColumns.NAME + " IN (?)", packNames, null, null, null);
      if (res.getCount() >= 1) {
        res.moveToFirst();
        int oldVersion = res.getInt(3);
        int oldId = res.getInt(0);
        if (packVersion > oldVersion) {
          return oldId;
        } else {
          return PACK_CURRENT;
        }
      } else {
        return PACK_NOT_PRESENT;
      }
    }

    public static void clearPack(int packId, SQLiteDatabase db) {
      String[] whereArgs = new String[] { String.valueOf(packId) };
      db.delete(PackColumns.TABLE_NAME, PackColumns._ID + "=?", whereArgs);
      db.delete(PhraseColumns.TABLE_NAME, PhraseColumns.PACK_ID + "=?", whereArgs);
    }
    
    /**
     * Return the id a provided pack
     * @param packName A string that matches a pack for which an ID is needed
     * @return
     */
    public int getPackId(String packName) {
      Log.d(TAG, "getPackId(" + packName + ")");
      
      mDatabase = getWritableDatabase();
      
      // TODO: Question for code review.  Better to do a join or two separate 
      // Get our pack ID
      String[] name = {packName};
      Cursor res = mDatabase.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS, 
          PackColumns.NAME + " = ?", name, null, null, null);
      
      int packid = -1;
      if (res.moveToFirst()) {
        packid = res.getInt(0);
      }
      res.close();
      return packid;
    }
    /**
     * Update playdate for all passed in phrase ids to current time
     * @param ids
     *          comma delimited set of phrase ids to incrment, ex. "1, 2, 4, 10"
     * @return
     */
    public void updatePlaydate(LinkedList<Card> cardList) {
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "updatePlaydate()");
      }
      
      String ids = buildCardIdString(cardList);
      
      //TODO for code review:  For some reason the database.update command was interpreting the
      // playcount+1 as a string and inserting that string instead of actually incrementing
      // If its all the same to everyone, I went ahead and hardcoded this query to work around this
      // There is also a known issue with this...if a card is seen twice in a single round
      // it will only get updated once.  To me the issue of seeing a card twice is the real issue
      // and it's not worth fixing the count for that severe case.
//      ContentValues newValues = new ContentValues();
//      newValues.put("playcount", "playcount+1");
//      mDatabase.update(PHRASE_TABLE_NAME, newValues, "id in (" + args + ")", null);
      mDatabase.execSQL("UPDATE " + PhraseColumns.TABLE_NAME
                      + " SET " + PhraseColumns.PLAY_DATE + " = datetime('now')"
                      + " WHERE " + PhraseColumns._ID + " in(" + ids + ");");
      close();
    }

    /**
     * Generates and returns a LinkedList of Cards from the database for a specific pack.  First,
     * we request all the cards from the db sorted by date.  Then we calculate how many of the 
     * Cards should be returned based on the pack's weight relative to the total number of selected
     * cards.  Then, just to shake things up we take a few extra, and remove an equal number at random
     * @param packname
     * @param LACK
     * @param TOTAL_SELECTED
     * @return
     */
    public LinkedList<Card> pullFromPack(Pack pack, LinkedList<Card> cardsToExclude, 
                                          int targetNum) {
      Log.d(TAG, "pullFromPack(" + pack.getName() + ")");
      mDatabase = getWritableDatabase();
      
      LinkedList<Card> returnCards = new LinkedList<Card>();
      int packid = pack.getId();
      
      String[] args = new String[3];
      args[0] = String.valueOf(packid);
      args[1] = buildCardIdString(cardsToExclude);
      args[2] = buildDifficultyString();
      Log.d(TAG, "-- exclude str: " + args[1]);
      // Get the phrases from pack, sorted by playdate, and no need to get more than the CACHE_SIZE      
      Cursor res = mDatabase.query(PhraseColumns.TABLE_NAME, PhraseColumns.COLUMNS,
          PhraseColumns.PACK_ID + " = " + args[0] + " AND " + PhraseColumns._ID + " NOT IN (" + args[1] + ") AND " +
          PhraseColumns.DIFFICULTY + " IN (" + args[2] + ")", null, 
          null, null, PhraseColumns.PLAY_DATE + " asc");
      res.moveToFirst();
      
      // The number of cards to return from any given pack will use the following formula:
      // (WEIGHT OF PACK) * CACHE_SIZE + SURPLUS --> Then we randomly take out X cards where X = SURPLUS
      int packSize = res.getCount();
      int surplusNum = (int) Math.floor( (float) targetNum * ((float) Deck.THROW_BACK_PERCENTAGE / 100.00));

      Log.d(TAG, "** back cache size: " + Deck.BACK_CACHE_MAXSIZE);      
      Log.d(TAG, "** pack size: " + packSize);
      Log.d(TAG, "** targetnum: " + targetNum);
      Log.d(TAG, "** surplusnum: " + surplusNum);
      
      Log.d(TAG, "** ADDING PHRASES");

      // Add cards to what will be the Cache, including a surplus 
      while (!res.isAfterLast() && res.getPosition() < (targetNum + surplusNum)) {
        returnCards.add(new Card(res.getInt(0), res.getString(1), res.getInt(2)));
        res.moveToNext();
      }
      Log.d(TAG, "**" + returnCards.size() + " phrases added.");
      
      // Throw out x surplus cards at random
      Random r = new Random();
      int removeCount = 0;
      int randIndex = 0;
      Log.d(TAG, "** REMOVING PHRASES");
      while (removeCount < surplusNum) {
        randIndex = r.nextInt(returnCards.size()-1);
        Log.d(TAG, "**removing: " + returnCards.get(randIndex).getTitle());
        returnCards.remove(randIndex);
        removeCount++;
      }
      Log.d(TAG, "**" + removeCount + " phrases removed.");
      
      res.close();
      return returnCards;
    }
    
    /**
     * Helper class to convert a linked list of cards to a 
     * comma-delimited string of card Ids.
     * @param cardList
     * @return cardIds - 
     */
    private String buildCardIdString(LinkedList<Card> cardList) {
      Log.d(TAG, "buildCardIdString(cardList)");
      
      String cardIds = "";
      //TODO JOIN THIS
      for (int i=0; i< cardList.size(); ++i) {
        // Protect against packs not found (-1 is ID returned)
        cardIds += (String.valueOf(cardList.get(i).getId()));
        if (i < cardList.size()-1) {
          cardIds += (",");
        }
      }
      return cardIds;
    }
    
    private String buildPackIdString(LinkedList<Pack> packList) {
      Log.d(TAG, "buildPackIdString(packList)");
      String[] ids = new String[packList.size()];
      for (int i=0; i<packList.size(); ++i) {
        ids[i] = String.valueOf(packList.get(i).getId());
        Log.d(TAG, "** " + ids[i]);
      }
      String ret = TextUtils.join(",", ids);
      return ret;
    }
    
    /**
     * Helper method to build a comma-delimited string of the enabled
     * difficulties
     * @return Comma-delimited string of difficulties for db args
     */
    private String buildDifficultyString() {
      Log.d(TAG, "buildDifficultyString()");
      SharedPreferences prefs = PreferenceManager
          .getDefaultSharedPreferences(mHelperContext);
      
      Boolean easy = prefs.getBoolean("easy_phrases", true);
      Boolean medium = prefs.getBoolean("medium_phrases", true);
      Boolean hard = prefs.getBoolean("hard_phrases", true);
      
      String ret = "";
      if (easy && medium && hard) {
        ret = "0,1,2";
      } else if (easy && hard) {
        ret = "0,2";
      } else if (easy && medium) {
        ret = "0,1";
      } else if (medium && hard) {
        ret = "1,2";
      } else if (easy) {
        ret = "0";
      } else if (medium) {
        ret = "1";
      } else if (hard) {
        ret = "2";
      }
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
      // TODO Handle Upgrades (here may not be relevant).
      db.execSQL("DROP TABLE IF EXISTS phrases;");
      db.execSQL("DROP TABLE IF EXISTS packs;");
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
