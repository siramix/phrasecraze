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
import java.util.Iterator;

import org.json.JSONException;

/**
 * Iterator for processing a JSON-filled buffered reader into cards. This is
 * helpful for processing large JSON object collections in a sane, yet
 * efficient, manner.
 *
 * DO NOT FORGET TO CALL "close()" WHEN YOU'RE DONE.
 *
 * Aside: I have some reservations about doing exception handling at this
 * level, but the API of Iterator ties us down to handling it here. We should
 * do some performance testing to assess the impact.
 * @author Siramix Labs
 */
public class CardJSONIterator implements Iterator<Card> {

  /**
   * The internal buffered reader
   */
  private BufferedReader mReader;

  /**
   * Always construct with a valid buffered reader.
   * @param reader
   */
  public CardJSONIterator(BufferedReader reader) {
    mReader = reader;
  }

  /**
   * @return true if the buffered reader is "ready"
   */
  public boolean hasNext() {
    try {
      return mReader.ready();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Pull a line off of the buffered reader and parse it into a card. If the
   * JSON is malformed or the reader is in a bad state the card will be null.
   * @return the card from the reader or null if an exception occurs
   */
  public Card next() {
    String strCard;
    Card card;
    try {
      strCard = mReader.readLine();
      card = PackParser.stringToCard(strCard);
    } catch (IOException e) {
      card = null;
      e.printStackTrace();
    } catch (JSONException e) {
      card = null;
      e.printStackTrace();
    }
    return card;
  }

  /**
   * Phrasecraze will not use this, but if we want to remove a card from the
   * incoming stream, we just burn a line off the reader.
   */
  public void remove() {
    try {
      mReader.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Close the buffered reader. This is more important than you think it is :)
   */
  public void close() {
    try {
      mReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
