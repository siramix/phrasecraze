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

import java.io.IOException;
import java.io.InputStream;
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
  private static final int DATABASE_VERSION = 2;
  private static final int CACHE_SIZE = 50;
  private static final String PHRASE_TABLE_CREATE = "CREATE TABLE "
      + PHRASE_TABLE_NAME + "( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
      + "phrase TEXT, " + "difficulty INTEGER, "
      + "playcount INTEGER, " + "pack_id INTEGER, "
      + "FOREIGN KEY(pack_id) REFERENCES pack(id) );";
  private static final String CACHE_TABLE_CREATE = "CREATE TABLE "
      + CACHE_TABLE_NAME + "( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
      + "val TEXT );";
  private static final String PACK_TABLE_CREATE = "CREATE TABLE " 
      + PACK_TABLE_NAME + "( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
      + "packname TEXT );";

  private static final String[] PHRASE_COLUMNS = { "id", "phrase", "difficulty", 
                                                   "playcount", "pack_id" };
  private static final String[] PACK_COLUMNS = { "id", "packname" };
  private static final String[] CACHE_COLUMNS = { "id", "val" };
  private LinkedList<Card> mCache;
  private int mSeed;
  private int mPosition;
  private ArrayList<Integer> mOrder;
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
    mCache = new LinkedList<Card>();
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(context);
    Random r = new Random();
    mSeed = sp.getInt("deck_seed", r.nextInt());
    r = new Random(mSeed);
    Editor editor = sp.edit();
    editor.putInt("deck_seed", mSeed);
    editor.commit();
    mPosition = sp.getInt("deck_position", 0);
    int sizeOfDeck = mDatabaseOpenHelper.countPhrases();
    mOrder = new ArrayList<Integer>(sizeOfDeck);
    for (int i = 0; i < sizeOfDeck; ++i) {
      mOrder.add(i);
    }
    Collections.shuffle(mOrder, r);
    mCache = mDatabaseOpenHelper.loadCache();
    mDatabaseOpenHelper.close();
  }

  /**
   * Prepare for a round by caching the cards necessary for that round (get the
   * number of cards back up to DECK_SIZE). Refilling the cache
   */
  public void prepareForRound() {
    mDatabaseOpenHelper = new DeckOpenHelper(mContext);
    
    // Find out the indices we need to fill the cache
    int lack = CACHE_SIZE - mCache.size();
    String ids = "";
    for (int i = 0; i < lack; ++i) {
      if (mPosition >= mOrder.size()) {
        mPosition = 0;
      }

      ids += mOrder.get(mPosition++);
      if (i < (lack - 1)) {
        ids += ",";
      }

    }

    // Update our position in the application prefs
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(mContext);
    Editor editor = sp.edit();
    editor.putInt("deck_position", mPosition);
    editor.commit();

    // Fill the cache from the deck (DB)
    mCache.addAll(mDatabaseOpenHelper.getPhrases(ids));
    Collections.shuffle(mCache);
    mDatabaseOpenHelper.saveCache(mCache);
    mDatabaseOpenHelper.close();
  }

  /**
   * Get the phrase from the top of the cache
   * 
   * @return a card reference
   */
  public Card getPhrase() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getPhrase()");
    }
    if (mCache.isEmpty()) {
      this.prepareForRound();
      return mCache.removeFirst();
    } else {
      return mCache.removeFirst();
    }
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
      loadPhrases(db);
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
     * Load the words from the XML file using only one SQLite database
     * 
     * @param db
     *          from the installing context
     */
    private void loadPhrases(SQLiteDatabase db) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Loading phrases...");
      }

      mDatabase = db;

      InputStream starterXML = mHelperContext.getResources().openRawResource(
          R.raw.starter);
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();

      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Building DocBuilderFactory for phrase pack parsing from "
            + R.class.toString());
      }
      try {
        mDatabase.beginTransaction();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(starterXML);
        NodeList phraseEntryNodes = doc.getElementsByTagName("phrase_entry");
        for (int i = 0; i < phraseEntryNodes.getLength(); i++) {
          NodeList packPhraseDif = phraseEntryNodes.item(i).getChildNodes();
          Node packNode = null;
          Node phraseNode = null;
          Node difficultyNode = null;
          for (int j = 0; j < packPhraseDif.getLength(); j++) {
            String candidateName = packPhraseDif.item(j).getNodeName();
            if (candidateName.equals("pack_id")) {
              packNode = packPhraseDif.item(j);
            } else if (candidateName.equals("phrase")) {
              phraseNode = packPhraseDif.item(j);
            } else if (candidateName.equals("difficulty")) {
              difficultyNode = packPhraseDif.item(j);
            }
            else {
              continue; // We found some #text
            }
          }
          int pack = Integer.parseInt(packNode.getFirstChild().getNodeValue());
          String phrase = phraseNode.getFirstChild().getNodeValue();
          int difficulty = Integer.parseInt(difficultyNode.getFirstChild().getNodeValue());          
          
          this.addPhrase(i, phrase, difficulty, pack, mDatabase);
        }
        mDatabase.setTransactionSuccessful();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        mDatabase.endTransaction();
      }

      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "DONE loading words.");
      }
    }

    /**
     * Add a phrase to the deck (DB)
     * 
     * @return rowId or -1 if failed
     */
    public long addPhrase(int id, String phrase, int difficulty, int packId, SQLiteDatabase db) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "addPhrase()");
      }
      ContentValues initialValues = new ContentValues();
      initialValues.put("id", id);
      initialValues.put("phrase", phrase);
      initialValues.put("difficulty", difficulty);
      initialValues.put("playcount", 0);
      initialValues.put("pack_id", packId);      
      return db.insert(PHRASE_TABLE_NAME, null, initialValues);
    }

    /**
     * Get the phrases corresponding to a comma-separated list of indices
     * 
     * @param args
     *          indices separated by commas
     * @return a reference to a linked list of cards corresponding to the ids
     */
    public LinkedList<Card> getPhrases(String args) {
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "getPhrases()");
      }
      Cursor res = mDatabase.query(PHRASE_TABLE_NAME, PHRASE_COLUMNS, "id in ("
          + args + ")", null, null, null, null);
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
      mDatabase = getWritableDatabase();
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "loadCache()");
      }
      Cursor res = mDatabase.query(CACHE_TABLE_NAME, CACHE_COLUMNS, "id in (0)", null,
          null, null, null);
      LinkedList<Card> ret;
      if (res.getCount() == 0) {
        ret = new LinkedList<Card>();
      } else {
        res.moveToFirst();
        ret = getPhrases(res.getString(1));
      }
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
