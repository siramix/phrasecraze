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

import android.provider.BaseColumns;

/**
 * This is a convenience class for defining the column and table names of
 * the important tables for the applications backing storage.
 * @author Siramix Labs
 */
public final class CardPackTypes {
  
  /**
   * This class cannot be instantiated
   */
  private CardPackTypes() {
  }
  
  /**
   * Card table contract
   */
  public static final class Cards implements BaseColumns {

    /**
     * This class cannot be instantiated
     */
    private Cards() {
    }
    
    public static final String TABLE_NAME = "cards";

    /**
     * Column name for the title of the card
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_TITLE = "title";

    /**
     * Column name of the card pack id
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_PACK = "pack_id";
  }
  
  /**
   * Pack table contract
   */
  public static final class Packs implements BaseColumns {

    /**
     * This class cannot be instantiated
     */
    private Packs() {
    }
    
    public static final String TABLE_NAME = "packs";

    /**
     * Column name for the name of the pack
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Column name of the pack enabled state
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_ENABLED = "enabled";
  }
  
  /**
   * Pack table contract
   */
  public static final class Categories implements BaseColumns {

    /**
     * This class cannot be instantiated
     */
    private Categories() {
    }
    
    public static final String TABLE_NAME = "categories";
    
    /**
     * Column name for the name of the category
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_NAME = "name";
  }
  
  /**
   * Pack table contract
   */
  public static final class CardToCategory implements BaseColumns {

    /**
     * This class cannot be instantiated
     */
    private CardToCategory() {
    }
    
    public static final String TABLE_NAME = "card2category";

    /*
     * Column definitions
     */

    /**
     * Column name for the id of the card
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_CARD = "card_id";
    
    /**
     * Column name for the id of the category
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_CATEGORY = "category_id";
  }
}
