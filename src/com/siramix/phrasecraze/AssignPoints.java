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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This handles changing the points scored for each team in a round
 * 
 * @author Siramix Labs
 */
public class AssignPoints extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "AssignPoints";

  /*
   * References to button views
   */
  private Button mButtonCancel;
  private Button mButtonAccept;
  private Button mButtonAddTeam1;
  private Button mButtonAddTeam2;
  private Button mButtonSubtractTeam1;
  private Button mButtonSubtractTeam2;
  
  /*
   * Members that track desired changes to scores
   */
  private int[] mScores;
  
  /*
   * Members for minimum and maximum scores for the round
   */
  private final int mMAXSCORE = 99;
  private final int mMINSCORE = -99;

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    mButtonCancel = (Button) this
        .findViewById(R.id.AssignPoints_ButtonCancel);
    mButtonAccept = (Button) this
        .findViewById(R.id.AssignPoints_ButtonConfirm);
    mButtonAddTeam1 = (Button) this.findViewById(R.id.AssignPoints_Team1_ButtonAdd);
    mButtonAddTeam2 = (Button) this.findViewById(R.id.AssignPoints_Team2_ButtonAdd);
    mButtonSubtractTeam1 = (Button) this.findViewById(R.id.AssignPoints_Team1_ButtonSubtract);
    mButtonSubtractTeam2 = (Button) this.findViewById(R.id.AssignPoints_Team2_ButtonSubtract);
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
      SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);
      
      finish();
    }
  };

  /**
   * Watches the button that handles returning to previous activity with changes
   * to scores
   */
  private final OnClickListener mAcceptListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Confirm onClick()");
      }

      // Pass back the new team scores
      Intent curIntent = new Intent();
      curIntent.putExtra(getString(R.string.assignedPointsBundleKey), mScores);
      AssignPoints.this.setResult(Activity.RESULT_OK, curIntent);
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
      
      finish();
    }
  };
  

  /**
   * Watches the add point button for team 1 and adds a point to their
   * round score when pressed.
   */
  private final OnClickListener mAddPointTeam1 = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "AddPointTeam1 onClick()");
      }

      if(mScores[0] < mMAXSCORE)
      {
    	  mScores[0] += 1;
    	  TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
    	  score.setText(Integer.toString(mScores[0]));

          // play confirm sound when points are added
          SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
          sm.playSound(SoundManager.Sound.CONFIRM);
      }
    }
  };
  
  /**
   * Watches the add point button for team 1 and adds a point to their
   * round score when pressed.
   */
  private final OnClickListener mAddPointTeam2 = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "AddPointTeam1 onClick()");
      }

      if(mScores[1] < mMAXSCORE)
      {
          mScores[1] += 1;
          TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
          score.setText(Integer.toString(mScores[1]));

          // play confirm sound when points are added
          SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
          sm.playSound(SoundManager.Sound.CONFIRM);
      } 
    }
  };
  
  /**
   * Watches the add point button for team 1 and subtracts a point from their
   * round score when pressed.
   */
  private final OnClickListener mSubtractPointTeam1 = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "AddPointTeam1 onClick()");
      }

      if (mScores[0] > mMINSCORE)
      {
    	  mScores[0] -= 1;
          TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
          score.setText(Integer.toString(mScores[0]));
          
          // play back sound when points are subtracted
          SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
          sm.playSound(SoundManager.Sound.BACK);
      }
    }
  };
  
  /**
   * Watches the add point button for team 1 and subtracts a point from their
   * round score when pressed.
   */
  private final OnClickListener mSubtractPointTeam2 = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "AddPointTeam1 onClick()");
      }

      if (mScores[1] > mMINSCORE)
      {
    	  mScores[1] -= 1;
          TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
          score.setText(Integer.toString(mScores[1]));      

          // play back sound when points are subtracted
          SoundManager sm = SoundManager.getInstance(AssignPoints.this.getBaseContext());
          sm.playSound(SoundManager.Sound.BACK);
      }
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

    this.setContentView(R.layout.assignpoints);
    
    setupViewReferences();

    // Set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    TextView label = (TextView) this.findViewById(R.id.AssignPoints_Title);
    label.setTypeface(antonFont);
    
    // Set fonts on scores
    TextView scoreView;
    scoreView = (TextView) this.findViewById(R.id.AssignPoints_Team1_Score);
    scoreView.setTypeface(antonFont);
    scoreView = (TextView) this.findViewById(R.id.AssignPoints_Team2_Score);
    scoreView.setTypeface(antonFont);
    
    // Get teams from application. This should maybe be passed in as a 
    // serializable list of teams
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
    		.getApplication();
    GameManager game = application.getGameManager();
    List<Team> teams = game.getTeams();
    Team team1 = teams.get(0);
    Team team2 = teams.get(1);
    
    // Initialize scores to the round score of each team
    mScores = new int[teams.size()];
    mScores[0] = team1.getRoundScore();
    mScores[1] = team2.getRoundScore();
    
    // Set initial values of each element based on its corresponding team
    TextView score = (TextView) this.findViewById(R.id.AssignPoints_Team1_Score);
    score.setText(Integer.toString(mScores[0]));
    score = (TextView) this.findViewById(R.id.AssignPoints_Team2_Score);
    score.setText(Integer.toString(mScores[1]));
    
    // Color backgrounds based on the two teams represented
    setupScoreboard((TextView) this.findViewById(R.id.AssignPoints_Team1_Name),
    		this.findViewById(R.id.AssignPoints_Team1_ScoreLayout), team1);
    setupScoreboard((TextView) this.findViewById(R.id.AssignPoints_Team2_Name),
    		this.findViewById(R.id.AssignPoints_Team2_ScoreLayout), team2);
    
    // Set listeners for buttons
    mButtonCancel.setOnClickListener(mCancelListener);
    mButtonAccept.setOnClickListener(mAcceptListener);
    mButtonAddTeam1.setOnClickListener(mAddPointTeam1);
    mButtonAddTeam2.setOnClickListener(mAddPointTeam2);
    mButtonSubtractTeam1.setOnClickListener(mSubtractPointTeam1);
    mButtonSubtractTeam2.setOnClickListener(mSubtractPointTeam2);
    
  }
  
  /**
   * Helper function to assign colors and visuals of the team related views
   * @param teamNameView - the view that houses the team's name
   * @param scoreBackground - the background view for the team's score
   * @param team - the team to be represented
   */
  private void setupScoreboard(TextView teamNameView, View scoreBackground, Team team)
  {
	  Typeface antonFont = Typeface.createFromAsset(getAssets(),
			  "fonts/Anton.ttf");
	  teamNameView.setBackgroundResource(team.getSecondaryColor());
	  teamNameView.setText(team.getName());
	  teamNameView.setTextColor(this.getResources().getColor(team.getPrimaryColor()));
	  teamNameView.setTypeface(antonFont);
	  scoreBackground.setBackgroundResource(team.getPrimaryColor());
  }
}
