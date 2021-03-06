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

import android.app.Application;
import android.util.Log;

/**
 * Class extending the standard android application. This allows us to refer to
 * one GameManager from every activity within PhraseCraze.
 * 
 * @author Siramix Labs
 */
public class PhraseCrazeApplication extends Application {
  /**
   * Global Debug constant
   */
  public static final boolean DEBUG = true;
  public static final boolean DEBUG_TIMERTICKS = false;

  /**
   * logging tag
   */
  public static String TAG = "PhraseCrazeApplication";

  /**
   * The GameManager for all of PhraseCraze
   */
  private GameManager mGameManager;

  /**
   * Default constructor
   */
  public PhraseCrazeApplication() {
    super();
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "PhraseCrazeApplication()");
    }
    mGameManager = null;
  }

  /**
   * @return a reference to the game manager
   */
  public GameManager getGameManager() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "GetGameManager()");
    }
    if(mGameManager == null) {
      mGameManager = new GameManager(this.getApplicationContext());
    }
    return this.mGameManager;
  }

  /**
   * @param gm
   *          - a reference to the game manager
   */
  public void setGameManager(GameManager gm) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "SetGameManager()");
    }
    this.mGameManager = gm;
  }

}
