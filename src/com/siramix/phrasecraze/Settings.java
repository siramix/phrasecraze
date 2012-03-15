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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Settings class handles the setting of exposed preferences
 * 
 * @author Siramix Labs
 */
public class Settings extends PreferenceActivity {
  
  /**
   * Watch the settings to update any changes (reset subtext, etc.)
   */
  private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        String key) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "onSharedPreferencesChanged()");
      }
      if (key.equals("turn_timer")) {
        // When turn timer is changed, update the caption
        Settings.this.updateTimerLabel();
      }
    }
  };

  /**
   * logging tag
   */
  public static String TAG = "Settings";

  /**
   * onCreate - initializes a settings screen
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.addPreferencesFromResource(R.xml.settings);

    // When turn timer is loaded, update the caption
    this.updateTimerLabel();

    // Update the version preference caption to the existing app version
    Preference version = findPreference("app_version");
    try {
      version.setTitle(this.getString(R.string.AppName));
      version
          .setSummary(" Version "
              + this.getPackageManager().getPackageInfo(this.getPackageName(),
                  0).versionName);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  /**
   * Updates the timer label by checking the preference for the current time
   */
  private void updateTimerLabel() {
    // When turn timer is loaded, update the caption
    ListPreference lp = (ListPreference) findPreference("turn_timer");
    lp.setSummary(lp.getEntry());
  }

  /**
   * Override onPause to prevent activity specific processes from running while
   * app is in background
   */
  @Override
  public void onPause() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onPause()");
    }
    super.onPause();

    // Unregister settings listeners
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    sp.unregisterOnSharedPreferenceChangeListener(mPrefListener);

  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onResume()");
    }
    super.onResume();

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    // Register preference listener with SharedPreferences
    sp.registerOnSharedPreferenceChangeListener(mPrefListener);

  }
}
