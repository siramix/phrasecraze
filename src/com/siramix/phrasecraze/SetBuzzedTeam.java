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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class allows the user to correct the game by assigning the team who had
 * the time run out on them manually.
 * 
 * @author Siramix Labs
 */
public class SetBuzzedTeam extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "SetBuzzedTeam";

  /*
   * References to button views
   */
  private Button mButtonCancel;
  private Button mButtonConfirm;
  
  // Store whether or not this dialog is cancellable, as passed in.
  private Boolean mIsChoiceRequired;
  
  // New buzzed team is whatever team is confirmed
  private Team mNewBuzzedTeam;
  // Old buzzed team is what it came in as
  private Team mOldBuzzedTeam;
  private List<Team> mTeams;

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    mButtonCancel = (Button) this
        .findViewById(R.id.SetBuzzedTeam_Buttons_Cancel);
    mButtonConfirm = (Button) this
        .findViewById(R.id.SetBuzzedTeam_Buttons_Confirm);
  }
  
  
  /**
   * Sets team1 as the Buzzed Team
   */
  private final OnClickListener mTeam1BuzzedListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Team1Buzzed onClick()");
      }
      onTeamClicked(mTeams.get(0));
    }
  };
  
  /**
   * Sets team2 as the Buzzed Team
   */
  private final OnClickListener mTeam2BuzzedListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Team2Buzzed onClick()");
      }
      onTeamClicked(mTeams.get(1));
    }
  };
  
  
  /**
   * Helper for the listeners that set the buzzed teams. Handles
   * the onClick event.
   */
  private void onTeamClicked(Team clickedTeam)
  {
    if(!clickedTeam.equals(mNewBuzzedTeam))
    {
      // play back sound
      SoundManager sm = SoundManager.getInstance(SetBuzzedTeam.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
      
      mNewBuzzedTeam = clickedTeam;
      
      mButtonConfirm.setEnabled(true);
      mButtonConfirm.setVisibility(View.VISIBLE);
      
      refreshTeamViews();
    }
  }
  

  /**
   * Watches the button that handles returning to previous activity with no
   * changes
   */
  private final OnClickListener mCancelListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Cancel onClick()");
      }

      // play back sound
      SoundManager sm = SoundManager.getInstance(SetBuzzedTeam.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);

      finish();
    }
  };

  /**
   * Watches the button that handles returning to previous activity with changes
   * to scores
   */
  private final OnClickListener mConfirmListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Confirm onClick()");
      }

      // Pass back the new buzzed team
      Intent curIntent = new Intent();
      curIntent.putExtra(getString(R.string.buzzedTeamBundleKey),
        mNewBuzzedTeam);
      SetBuzzedTeam.this.setResult(Activity.RESULT_OK, curIntent);
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(SetBuzzedTeam.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      finish();
    }
  };

  /**
   * Create the activity and display the card bundled in the intent.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    this.setContentView(R.layout.setbuzzedteam);
    Intent inIntent = getIntent();
    // Set whether or not this activity is cancellable
    mIsChoiceRequired = inIntent.getBooleanExtra(getApplication().getString(R.string.IntentCancellable), false);

    setupViewReferences();

    // Set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");
    TextView label = (TextView) this.findViewById(R.id.SetBuzzedTeam_Title);
    label.setTypeface(antonFont);

    // Get BuzzedTeam from bundle
    Bundle buzzedTeamBundle = inIntent.getExtras();
    mOldBuzzedTeam = (Team) buzzedTeamBundle.getSerializable(getString(R.string.buzzedTeamBundleKey));
    mNewBuzzedTeam = mOldBuzzedTeam;
    
    // Get Teams from Application. Perhaps these should these be passed in?
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    GameManager game = application.getGameManager();
    mTeams = game.getTeams();

    // Setup UI
    refreshTeamViews();
    
    // Setup views according to whether or not the dialog is cancellable
    if(mIsChoiceRequired)
    {
      mButtonCancel.setEnabled(false);
      mButtonCancel.setVisibility(View.INVISIBLE);
    }
    // Disable Confirm until a selection has been made.
    if(mNewBuzzedTeam == null)
    {
      mButtonConfirm.setEnabled(false);
      mButtonConfirm.setVisibility(View.INVISIBLE);
    }
    
    // Bind listeners
    mButtonCancel.setOnClickListener(mCancelListener);
    mButtonConfirm.setOnClickListener(mConfirmListener);
    RelativeLayout teamGroup = (RelativeLayout) this.findViewById(R.id.SetBuzzedTeam_Team1);
    teamGroup.setOnClickListener(mTeam1BuzzedListener);
    teamGroup = (RelativeLayout) this.findViewById(R.id.SetBuzzedTeam_Team2);
    teamGroup.setOnClickListener(mTeam2BuzzedListener);
  }
  
  /**
   * Helper function that refreshes the team views based on new game data
   */
  private void refreshTeamViews()
  {
    Team team1 = mTeams.get(0);
    Team team2 = mTeams.get(1);
    setupTeamViews((TextView) this.findViewById(R.id.SetBuzzedTeam_Team1_Name),
        this.findViewById(R.id.SetBuzzedTeam_Team1),
        (ImageView) this.findViewById(R.id.SetBuzzedTeam_Team1_RowEnd),
        (ImageView) this.findViewById(R.id.SetBuzzedTeam_Team1_RowEndStamp),
        team1, team1.equals(mNewBuzzedTeam));
    setupTeamViews((TextView) this.findViewById(R.id.SetBuzzedTeam_Team2_Name),
        this.findViewById(R.id.SetBuzzedTeam_Team2),
        (ImageView) this.findViewById(R.id.SetBuzzedTeam_Team2_RowEnd),
        (ImageView) this.findViewById(R.id.SetBuzzedTeam_Team2_RowEndStamp),
        team2, team2.equals(mNewBuzzedTeam));
  }

  /**
   * Helper function to assign colors and visuals of the team related views
   * 
   * @param teamNameView
   *          - the view that houses the team's name
   * @param scoreBackground
   *          - the background view for the team's score
   * @param team
   *          - the team to be represented
   */
  private void setupTeamViews(TextView teamNameView, View teamBackground,
      ImageView endPiece, ImageView stamp, Team team, Boolean isTeamBuzzed) {
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");
    teamNameView.setText(team.getName());
    teamNameView.setTextColor(this.getResources().getColor(
        team.getSecondaryColor()));
    teamNameView.setTypeface(antonFont);
    teamBackground.setBackgroundColor(this.getResources().getColor(
        team.getPrimaryColor()));
    endPiece.setColorFilter(this.getResources().getColor(team.getComplementaryColor()), Mode.MULTIPLY);
    if (isTeamBuzzed) {
      stamp.setVisibility(View.VISIBLE);
    } else {
      stamp.setVisibility(View.INVISIBLE);
    }
  }
  

  /**
   * Start tracking the back button so we can properly handle catching it in the
   * onKeyUp
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      event.startTracking();
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Do not allow the user to go back with the back button from this activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    // Make back do nothing on key-up instead of climb the action stack
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() && mIsChoiceRequired) {
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }
}
  
