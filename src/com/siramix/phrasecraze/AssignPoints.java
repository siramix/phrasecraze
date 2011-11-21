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

      mScores[0] += 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
      score.setText(Integer.toString(mScores[0]));
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

      mScores[1] += 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
      score.setText(Integer.toString(mScores[1]));
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

      mScores[0] -= 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
      score.setText(Integer.toString(mScores[0]));
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

      mScores[1] -= 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
      score.setText(Integer.toString(mScores[1]));
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
    TextView nameTeam1 = (TextView) this.findViewById(R.id.AssignPoints_Team1_Name);
    nameTeam1.setBackgroundResource(team1.getPrimaryColor());
    nameTeam1.setText(team1.getName());
    nameTeam1.setTextColor(this.getResources().getColor(team1.getSecondaryColor()));
    View bgTeam1 = this.findViewById(R.id.AssignPoints_Team1_ScoreLayout);
    bgTeam1.setBackgroundResource(team1.getPrimaryColor());
    
    TextView nameTeam2 = (TextView) this.findViewById(R.id.AssignPoints_Team2_Name);
    nameTeam2.setBackgroundResource(team2.getPrimaryColor());
    nameTeam2.setText(team2.getName());
    nameTeam2.setTextColor(this.getResources().getColor(team2.getSecondaryColor()));
    View bgTeam2 = this.findViewById(R.id.AssignPoints_Team2_ScoreLayout);
    bgTeam2.setBackgroundResource(team2.getPrimaryColor());
    
    // Set listeners for buttons
    mButtonCancel.setOnClickListener(mCancelListener);
    mButtonAccept.setOnClickListener(mAcceptListener);
    mButtonAddTeam1.setOnClickListener(mAddPointTeam1);
    mButtonAddTeam2.setOnClickListener(mAddPointTeam2);
    mButtonSubtractTeam1.setOnClickListener(mSubtractPointTeam1);
    mButtonSubtractTeam2.setOnClickListener(mSubtractPointTeam2);
    
  }
}
