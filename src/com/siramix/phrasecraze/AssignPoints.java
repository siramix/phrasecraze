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
  private int mScoreTeam1;
  private int mScoreTeam2;

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

      // Pass back the team and the name
      /*
      Intent curIntent = new Intent();
      curIntent.putExtra(getString(R.string.teamBundleKey), mTeam);
      curIntent.putExtra(getString(R.string.teamNameBundleKey), teamName);
      AssignPoints.this.setResult(Activity.RESULT_OK, curIntent);
      */
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

      mScoreTeam1 += 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
      score.setText(Integer.toString(mScoreTeam1));
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

      mScoreTeam2 += 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
      score.setText(Integer.toString(mScoreTeam2));
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

      mScoreTeam1 -= 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team1_Score);
      score.setText(Integer.toString(mScoreTeam1));
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

      mScoreTeam2 -= 1;
      TextView score = (TextView) AssignPoints.this.findViewById(R.id.AssignPoints_Team2_Score);
      score.setText(Integer.toString(mScoreTeam2));
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

    // Todo: Scores should initialize to the round scores of each team.
    mScoreTeam1 = 0;
    mScoreTeam2 = 0;
    
    setupViewReferences();
    
    // Set initial values of each element based on its corresponding team
    TextView score = (TextView) this.findViewById(R.id.AssignPoints_Team1_Score);
    score.setText(Integer.toString(mScoreTeam1));
    score = (TextView) this.findViewById(R.id.AssignPoints_Team2_Score);
    score.setText(Integer.toString(mScoreTeam2));
    

    // Set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    TextView label = (TextView) this.findViewById(R.id.AssignPoints_Title);
    label.setTypeface(antonFont);
    
    // Set fonts on scores
    score = (TextView) this.findViewById(R.id.AssignPoints_Team1_Score);
    score.setTypeface(antonFont);
    score = (TextView) this.findViewById(R.id.AssignPoints_Team2_Score);
    score.setTypeface(antonFont);
    
/*
    // Get the team from the passed in Bundle
    Intent curIntent = this.getIntent();
    Bundle teamBundle = curIntent.getExtras();
    mTeam = (Team) teamBundle
        .getSerializable(getString(R.string.teamBundleKey));
*/

    // Set listeners for buttons
    mButtonCancel.setOnClickListener(mCancelListener);
    mButtonAccept.setOnClickListener(mAcceptListener);
    mButtonAddTeam1.setOnClickListener(mAddPointTeam1);
    mButtonAddTeam2.setOnClickListener(mAddPointTeam2);
    mButtonSubtractTeam1.setOnClickListener(mSubtractPointTeam1);
    mButtonSubtractTeam2.setOnClickListener(mSubtractPointTeam2);
    
  }
}
