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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * This is the activity class that kicks off PhraseCraze and displays a nice title
 * with basic menu options
 * 
 * @author Siramix Labs
 */
public class Title extends Activity {
  
  /**
   * logging tag
   */
  public static String TAG = "Title";


  /**
   * Dialog constant for first Rate Us message
   */
  static final int DIALOG_RATEUS_FIRST = 0;
 
  /**
   * Dialog constant for second Rate Us message
   */
  static final int DIALOG_RATEUS_SECOND = 1;

  /**
   * PlayGameListener plays an animation on the view that will result in
   * launching GameSetup
   */
  private OnClickListener mPlayGameListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "PlayGameListener OnClick()");
      }

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(Title.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(Title.this.getApplication().getString(
          R.string.IntentPackPurchase), getIntent().getData()));
    }
  };
  

  /**
   * PlayGameListener plays an animation on the view that will result in
   * launching GameSetup
   */
  private OnClickListener mPhrasesListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "PhrasesListener OnClick()");
      }

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(Title.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(Title.this.getApplication().getString(
          R.string.IntentPackPurchase), getIntent().getData()));
    }
  };

  /**
   * Listener to determine when the settings button is clicked. Includes an
   * onClick function that starts the settingsActivity.
   */
  private OnClickListener mSettingsListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "SettingsListener OnClick()");
      }

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(Title.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(Title.this.getApplication().getString(
          R.string.IntentSettings), getIntent().getData()));
    }
  }; // End SettingsListener

  /**
   * Listener to determine when the Rules button is clicked on the title screen.
   * Includes an onClick method that will start the Rule activity.
   */
  private OnClickListener mRulesListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "RulesListener OnClick()");
      }
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(Title.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(
          getApplication().getString(R.string.IntentRules), getIntent()
              .getData()));

    }
  }; // End RulesListener

  /**
   * Listener to determine when the About Us button is clicked on the title
   * screen. Includes an onClick method that will start the Rule activity.
   */
  private OnClickListener mAboutUsListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "AboutUsListener OnClick()");
      }

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(Title.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      Uri uri = Uri.parse("http://www.siramix.com/");
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      startActivity(intent);
    }
  }; // End AboutUsListener

  /*
   * Returns the rotation animation for the starburst
   */
  private RotateAnimation rotateStarburst()
  {
    RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    rotate.setDuration(60000);
    rotate.setInterpolator(new LinearInterpolator());
    rotate.setRepeatCount(Animation.INFINITE);
    return rotate;
  }
  
  /**
   * Initializes a welcome screen that starts the game.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Capture our play count to decide whether to show the Rate Us dialog
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
    int playCount = sp.getInt(getResources().getString(R.string.PREFKEY_PLAYCOUNT), 0);
    boolean showReminder = sp.getBoolean(getResources().getString(R.string.PREFKEY_SHOWREMINDER), false);
    
    // If 3 plays have been done and reminder is not muted, show dialog
    if (showReminder) {
      if (playCount < 6) {
        showDialog(DIALOG_RATEUS_FIRST);
      }
      else {
        showDialog(DIALOG_RATEUS_SECOND);
      }
    }

    // Setup the Main Title Screen view
    this.setContentView(R.layout.title);

    // Assign listeners to the buttons
    ImageButton playButton = (ImageButton) this
        .findViewById(R.id.Title_Button_Play);
    playButton.setOnClickListener(mPlayGameListener);

    ImageButton settingsButton = (ImageButton) this
        .findViewById(R.id.Title_Button_Settings);
    settingsButton.setOnClickListener(mSettingsListener);
    
    ImageButton packsButton = (ImageButton) this
        .findViewById(R.id.Title_Button_Packs);
    packsButton.setOnClickListener(mPhrasesListener);
    
    ImageButton rulesButton = (ImageButton) this
        .findViewById(R.id.Title_Button_Rules);
    rulesButton.setOnClickListener(mRulesListener);
    
    ImageButton aboutusButton = (ImageButton) this
        .findViewById(R.id.Title_AboutUs);
    aboutusButton.setOnClickListener(mAboutUsListener);
    
    // Rotate the starburst
    ImageView starburst = (ImageView) this.findViewById(R.id.Title_Starburst);
    starburst.startAnimation(rotateStarburst());
  }

  /**
   * Sets the boolean preference for muting the Rate Us dialog to true.
   */
  private void delayRateReminder()
  {
	if (PhraseCrazeApplication.DEBUG) {
		Log.d(TAG, "muteRateReminder()");
	}
    // Prepare to edit preference for mute reminder bool
    PhraseCrazeApplication application = (PhraseCrazeApplication) getApplication();    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext());
    SharedPreferences.Editor prefEditor = sp.edit();   
    
    prefEditor.putBoolean(this.getResources().getString(R.string.PREFKEY_SHOWREMINDER), false);
    prefEditor.commit();    
  }
  
  /**
   * Sets the boolean preference for muting the Rate Us dialog to true.
   */
  private void muteRateReminder()
  {
	if (PhraseCrazeApplication.DEBUG) {
		Log.d(TAG, "muteRateReminder()");
	}
    // Prepare to edit preference for mute reminder bool
    PhraseCrazeApplication application = (PhraseCrazeApplication) getApplication();    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext());
    SharedPreferences.Editor prefEditor = sp.edit();   
    
    // 7 and false will mean the user has seen the second dialog and muted it 
    prefEditor.putInt(this.getResources().getString(R.string.PREFKEY_PLAYCOUNT),7);
    prefEditor.putBoolean(this.getResources().getString(R.string.PREFKEY_SHOWREMINDER), false);
    
    prefEditor.commit();    
  }
  
  /**
   * Custom create Dialogs
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreateDialog(" + id + ")");
    }
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    /**
     * When players have played X times, show a dialog asking them to rate us or put it
     * off until later.  We will provide a 'Never' option as well.
     */
    case DIALOG_RATEUS_FIRST:
      builder = new AlertDialog.Builder(this);
      builder
          .setTitle(
              getResources().getString(R.string.rateUsFirstDialog_title))
          .setMessage(     		  
        	  getResources().getString(R.string.rateUsFirstDialog_text))
          .setPositiveButton(getResources().getString(R.string.rateUsDialog_positiveBtn), 
        		  new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {              
	                  Uri uri = Uri.parse(getResources().getString(R.string.rateUs_URI));
	                  Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	                  startActivity(intent);
	                  muteRateReminder();
	        }
          }).setNegativeButton(getResources().getString(R.string.rateUsDialog_neutralBtn),
	              new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                  delayRateReminder();
	                }
	        }
            );
      dialog = builder.create();
      break;
    
    /**
     * The second more urgent dialog shows after 6 plays
     */
    case DIALOG_RATEUS_SECOND:
        builder = new AlertDialog.Builder(this);
        builder
            .setTitle(
                getResources().getString(R.string.rateUsSecondDialog_title))
            .setMessage(
            	getResources().getString(R.string.rateUsSecondDialog_text))
            .setPositiveButton(getResources().getString(R.string.rateUsDialog_positiveBtn), 
            	  new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {              
		              Uri uri = Uri.parse(getResources().getString(R.string.rateUs_URI));
		              Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		              startActivity(intent);
		              muteRateReminder();
              }
            }).setNegativeButton(getResources().getString(R.string.rateUsDialog_negativeBtn), 
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      muteRateReminder();
              }
            });
      dialog = builder.create();
      break;
      
    default:
      dialog = null;
    }
    return dialog;
  }
  
}
