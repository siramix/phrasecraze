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
import android.content.Intent;
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
public class PackInfo extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "PackInfo";

  /*
   * References to button views
   */
  private Button mButtonCancel;
  private Button mButtonAccept;
  
  /**
   * References to elements that render pack data
   */
  private PackPurchaseRowLayout mPackTitle;
  private TextView mPackDescription;
  private ComboPercentageBar mPhraseCountBar;
  private TextView mPackIsOwnedText;
  
  /*
   * Reference to the pack this activity is displaying
   */
  private Pack mPack;
  private boolean mIsPackSelected;
  private boolean mIsPackRowOdd;
  private boolean mIsPackPurchased;

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    mButtonCancel = (Button) this
        .findViewById(R.id.PackInfo_Buttons_Cancel);
    mButtonAccept = (Button) this
        .findViewById(R.id.PackInfo_Buttons_Accept);
    mPackDescription = (TextView) this.findViewById(R.id.PackInfo_Description);
    mPackTitle = (PackPurchaseRowLayout) this.findViewById(R.id.PackInfo_TitlePackRow);
    mPhraseCountBar = (ComboPercentageBar) this.findViewById(R.id.PackInfo_PhraseCountBar);
    mPackIsOwnedText = (TextView) this.findViewById(R.id.PackInfo_AlreadyOwnedText);
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
      SoundManager sm = SoundManager.getInstance(PackInfo.this.getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);
      
      finish();
    }
  };

  /**
   * Watches the button that handles generic OK
   */
  private final OnClickListener mAcceptListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Confirm onClick()");
      }
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PackInfo.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
      
      finish();
    }
  };
  

  /**
   * Watches the button that handles pack purchase
   */
  private final OnClickListener mBuyListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Buy onClick()");
      }
      /*
      // Bind Listener
      //TODO this will need to be more specific later (to just free social apps)
      // Attach Twitter Listener
      if (mPack.getId() == 4 && mPack.isInstalled() == false) {
        row.setOnClickListener(mTweetListener);
        mSocialPacks.put(TWITTER_REQUEST_CODE, curPack);
      }
      // Attach Facebook Listener
      else if (curPack.getId() == 5 && curPack.isInstalled() == false) {
        row.setOnClickListener(mFacebookListener);
        mSocialPacks.put(FACEBOOK_REQUEST_CODE, curPack);
      }
      // Attach Google + Listener
      else if (curPack.getId() == 6 && curPack.isInstalled() == false) {
        row.setOnClickListener(mGoogleListener);
        mSocialPacks.put(GOOGLEPLUS_REQUEST_CODE, curPack);
      }
      */
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PackInfo.this.getBaseContext());
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

    this.setContentView(R.layout.packinfo);
    Intent inIntent = getIntent();
    
    // Set references to passed-in data
    mPack = (Pack) inIntent.getExtras().get(
        getApplication().getString(R.string.packBundleKey));
    mIsPackSelected = inIntent.getBooleanExtra(
        getApplication().getString(R.string.packInfoIsPackSelectedBundleKey),
        false);
    mIsPackRowOdd = inIntent.getBooleanExtra(
        getApplication().getString(R.string.packInfoIsPackRowOddBundleKey),
        false);
    mIsPackPurchased = inIntent.getBooleanExtra(
        getApplication().getString(R.string.packInfoIsPackPurchased),
        false);

    setupViewReferences();

    setupPackDataViews();
    
    setupButtons();
  }
  
  /*
   * Populate the elements with the Pack's Data
   */
  private void setupPackDataViews()
  {
    mPackTitle.setPack(mPack, mIsPackSelected, mIsPackRowOdd);
    mPackTitle.setRowClickable(false);
    mPackDescription.setText(mPack.getDescription());
    //mPhraseCountBar.setTitle(Integer.toString(mPack.getNumPlayablePhrases()));
    mPhraseCountBar.setTitle("Phrases in Pack: 500");
    int numEasyPhrases = 150;
    int numMediumPhrases = 250;
    int numHardPhrases = 50;
    int rowAndLabelColor = this.getResources().getColor(R.color.packInfo_EasyPhrases);
    mPhraseCountBar.setSegmentComponents(0, numEasyPhrases, "Easy", rowAndLabelColor, rowAndLabelColor);
    rowAndLabelColor = this.getResources().getColor(R.color.packInfo_MediumPhrases);
    mPhraseCountBar.setSegmentComponents(1, numMediumPhrases, "Medium", rowAndLabelColor, rowAndLabelColor);
    rowAndLabelColor = this.getResources().getColor(R.color.packInfo_HardPhrases);
    mPhraseCountBar.setSegmentComponents(2, numHardPhrases, "Hard", rowAndLabelColor, rowAndLabelColor);

    mPhraseCountBar.updateSegmentWeights();
  }

 
  /* 
   * Setup the view and buttons based on the purchasability of the pack
   */
  private void setupButtons()
  {
    if(!mIsPackPurchased)
    {
      mButtonCancel.setVisibility(View.VISIBLE);
      mPackIsOwnedText.setVisibility(View.GONE);
      mButtonAccept.setText(this.getResources().getString(R.string.packInfo_confirm_buy));
      mButtonAccept.setOnClickListener(mBuyListener);
      mButtonCancel.setOnClickListener(mCancelListener);
    }
    else
    {
      mButtonCancel.setVisibility(View.GONE);
      mPackIsOwnedText.setVisibility(View.VISIBLE);
      mButtonAccept.setText(this.getResources().getString(R.string.packInfo_confirm_nobuy));
      mButtonAccept.setOnClickListener(mAcceptListener);
    }
  }
}
