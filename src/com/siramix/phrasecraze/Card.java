package com.siramix.phrasecraze;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.util.Log;

/**
 * The Card class is a simple container class for storing data associated with
 * cards
 * 
 * @author Siramix Labs
 */
public class Card implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -5094548104192852941L;

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Card";

  /**
   * R-W-S Constants
   */
  public static final int NOTSET = -1;
  public static final int RIGHT = 0;
  public static final int WRONG = 1;
  public static final int SKIP = 2;

  /**
   * The db id of the card
   */
  private int mId;

  /**
   * The right,wrong,skip {0,1,2} state of the card
   */
  private int mRws;

  /**
   * The title of the card, the word to be guessed
   */
  private String mTitle;

  /**
   * Function for breaking a string into an array list of strings based on the
   * presence of commas. The bad words are stored in the database as a comma
   * separated list for each card.
   * 
   * @param commaSeparated
   *          - a comma separated string
   * @return an array list of the substrings
   */
  public static ArrayList<String> bustString(String commaSeparated) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "BustString()");
    }
    ArrayList<String> ret = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer(commaSeparated);

    while (tok.hasMoreTokens()) {
      ret.add(tok.nextToken(",").toUpperCase());
    }

    return ret;
  }

  /**
   * Get the resource ID for this card's right wrong skip icon Mid-turn (when
   * user hits back). These IDs must differ from those on Turn Result Screen.
   */
  public static int getCardMarkDrawableId(int cardRWS) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getDrawableId()");
    }
    switch (cardRWS) {
    case RIGHT:
      return R.drawable.stamp_right;
    case WRONG:
      return R.drawable.stamp_wrong;
    case SKIP:
      return R.drawable.stamp_skip;
    default:
      return 0;
    }
  }

  /**
   * Default constructor
   */
  public Card() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "Card()");
    }
    this.init(NOTSET, NOTSET, "");
  }

  /**
   * Copy Constructor
   */
  public Card(Card rhs) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "Card( Card )");
    }
    this.init(rhs.getId(), rhs.getRws(), rhs.getTitle());
  }

  /**
   * Standard constructor accepting all members as their native types
   * 
   * @param id
   * @param rws
   * @param title
   * @param badWords
   */
  public Card(int id, int rws, String title) {
    this.init(id, rws, title);
  }

  /**
   * Equals function for comparison
   */
  @Override
  public boolean equals(Object compareObj) {
    if (this == compareObj) {
      return true;
    }

    if (compareObj == null) {
      return false;
    }

    if (!(compareObj instanceof Card)) {
      return false;
    }
    Card rhs = (Card) compareObj;
    return mRws == rhs.getRws() && mTitle.equals(rhs.getTitle());
  }

  /**
   * Function for initializing card state
   */
  private void init(int id, int rws, String title) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "init()");
    }
    mId = id;
    mRws = rws;
    mTitle = title;
  }

  /**
   * Get the right/wrong/skip state as an integer
   * 
   * @return
   */
  public int getRws() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getRws()");
    }
    return mRws;
  }

  /**
   * Set the right/wrong/skip state as an integer
   * 
   * @param rws
   */
  public void setRws(int rws) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setRws()");
    }
    mRws = rws;
  }

  /**
   * Get a reference to the title string
   * 
   * @return
   */
  public String getTitle() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getTitle()");
    }
    return mTitle;
  }

  /**
   * Set the title as a string
   * 
   * @param title
   */
  public void setTitle(String title) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setTitle()");
    }
    mTitle = title;
  }

  /**
   * Get the resource ID for this card's right wrong skip icon
   */
  public int getRowEndDrawableId() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getRowEndDrawableId()");
    }
    switch (mRws) {
    case 0:
      return R.drawable.right;
    case 1:
      return R.drawable.wrong;
    case 2:
      return R.drawable.skip;
    default:
      return 0;
    }
  }

  /**
   * Cycle right/wrong/skip for the turn summary
   */
  public void cycleRws() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "cycleRws()");
    }
    mRws++;
    if (mRws == 3) {
      mRws = 0;
    }
  }

  /**
   * Sets a card's id (from DB)
   * 
   * @param id
   */
  public void setId(int id) {
    mId = id;
  }

  /**
   * @return the id (DB)
   */
  public int getId() {
    return mId;
  }

}
