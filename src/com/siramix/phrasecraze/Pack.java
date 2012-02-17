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

import java.io.Serializable;
import java.util.LinkedList;

import android.util.Log;

/**
 * Helpful structure for holding pack metadata. You can also attach cards to
 * it, but that is not a requirement.
 * @author Siramix Labs
 */
public class Pack implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3764144456280008930L;
  private String mName;
  private String mUpdateMessage;
  private String mPath;
  private int mVersion;
  private int mSize;
  private LinkedList<Card> mCardList;

  private static final String TAG = "Pack";
  /**
  
  /**
   * Default constructor
   */
  public Pack() {
    this("","","",-1,-1);
    Log.d(TAG, "consructor Pack()");
  }

  /**
   * Standard constructor
   * @param name
   * @param updateMessage
   * @param path
   * @param version
   * @param size
   */
  public Pack( String name, String updateMessage, String path, int version, int size) {
    Log.d(TAG, "constructor Pack(args)");
    mName = name;
    mUpdateMessage = updateMessage;
    mPath = path;
    mVersion = version;
    mSize = size;
    mCardList = null;
  }

  /**
   * @return the name of the pack
   */
  public String getName() {
    Log.d(TAG, "getName()");
    return mName;
  }

  /**
   * @return the update message
   */
  public String getUpdateMessage() {
    Log.d(TAG, "getUpdateMessage()");    
    return mUpdateMessage;
  }

  /**
   * @return the path from server-root with which to retrieve cards.
   */
  public String getPath() {
    return mPath;
  }

  /**
   * @return the version of the pack
   */
  public int getVersion() {
    return mVersion;
  }

  /**
   * @return the number of cards that should be in the pack
   */
  public int getSize() {
    return mSize;
  }

  /**
   * @return the list of all cards attached to this pack (no promises).
   */
  public LinkedList<Card> getCardList() {
    if(mCardList == null) {
      mCardList = new LinkedList<Card>();
    }
    return mCardList;
  }

}
