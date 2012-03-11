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

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * This activity class is responsible for displaying the rules of phrasecraze to
 * the user.
 * 
 * @author Siramix Labs
 */
public class Rules extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "Rules";


  /**
   * onCreate - initializes the activity to display the rules.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.setContentView(R.layout.rules);

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());

    // String used for displaying the customizable preferences to the user
    StringBuilder prefBuilder = new StringBuilder();
    prefBuilder
        .append("(These rules can be changed any time from the Settings screen.)");

    // Turn Length rule display
    prefBuilder.append("\n\nTurn Length: " + sp.getString("turn_timer", "60")
        + " seconds");

    // Allow Skipping rule display
    if (sp.getBoolean("allow_skip", true)) {
      prefBuilder.append("\nAllow Skipping: Players may skip words.");
    } else {
      prefBuilder.append("\nAllow Skipping: Players can not skip words.");
    }
    TextView rulePrefs = (TextView) this.findViewById(R.id.Rules_Preferences);
    rulePrefs.setText(prefBuilder);
  }

}
