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
  private int mPurchaseType;
  
  /*
   * Results that can be returned from this activity
   */
  public static final int RESULT_TWITTER = 2;
  public static final int RESULT_FACEBOOK = 3;
  public static final int RESULT_GOOGLE = 4;
  
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
   * Watches the button that handles pack purchase
   */
  private final OnClickListener mAcceptListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Buy onClick()");
      }  

      Intent outIntent = new Intent();
      outIntent.putExtra(getString(R.string.packBundleKey), mPack);
      // Set result
      PackInfo.this.setResult((Integer) v.getTag(), outIntent);
      
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
    mPurchaseType = inIntent.getExtras().getInt("HACK_PurchaseType");

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
    
    mButtonAccept.setOnClickListener(mAcceptListener);
    if(!mIsPackPurchased)
    {
      mButtonCancel.setVisibility(View.VISIBLE);
      mButtonCancel.setOnClickListener(mCancelListener);
      mPackIsOwnedText.setVisibility(View.GONE);
      
      // Set Accept Button text and return result based on the purchase type
      switch (mPurchaseType)
      {
      case Pack.PURCHASETYPE_BUY:
        mButtonAccept.setText(this.getResources().getString(R.string.packInfo_confirm_buy));
        mButtonAccept.setTag(RESULT_OK);
        break;
      case Pack.PURCHASETYPE_TWEET:
        mButtonAccept.setText("Tweet");
        mButtonAccept.setTag(RESULT_TWITTER);
        break;
      case Pack.PURCHASETYPE_FACEBOOK:
        mButtonAccept.setText("Post");
        mButtonAccept.setTag(RESULT_FACEBOOK);
        break;
      case Pack.PURCHASETYPE_GOOGLE:
        mButtonAccept.setText("Plus+1");
        mButtonAccept.setTag(RESULT_GOOGLE);
        break;
      }
    }
    else
    {
      mButtonCancel.setVisibility(View.GONE);
      mPackIsOwnedText.setVisibility(View.VISIBLE);
      mButtonAccept.setText(this.getResources().getString(R.string.packInfo_confirm_nobuy));
      mButtonAccept.setOnClickListener(mAcceptListener);
      // Set the result to return for the button
      mButtonAccept.setTag(RESULT_CANCELED);
    }
  }
}
