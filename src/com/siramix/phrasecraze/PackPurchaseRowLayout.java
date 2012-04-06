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

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

/**
 * Custom view that represents a pack.
 * 
 * @author The PhraseCraze Team
 * 
 */
public class PackPurchaseRowLayout extends RelativeLayout {

  protected static final String TAG = "PackPurchaseRowLayout";

  private Context mContext;

  private FrameLayout mFrame;
  private LinearLayout mContents;
  private TextView mTitle;
  private TextView mPrice;
  private RelativeLayout mEndGroup;
  private CheckBox mCheckbox;
  private ImageView mRowEndBG;

  private Pack mPack;
  private boolean mIsPackEnabled;
  private boolean mIsRowOdd;
  
  /*
   * Listeners for click events on this row
   */
  private OnPackSelectedListener mPackSelectedListener;
  private OnPackInfoRequestedListener mPackInfoListener;
  // Allow users to disable the Selection listener
  private boolean mIsRowClickable; 
  
  /**
   * @param context
   */
  public PackPurchaseRowLayout(Context context) {
    super(context);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public PackPurchaseRowLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public PackPurchaseRowLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initializeMembers(context);
  }
  
  private void initializeMembers(Context context)
  {
    mContext = context;
    mFrame = new FrameLayout(mContext);
    mTitle = new TextView(mContext);
    mContents = new LinearLayout(mContext);
    mPrice = new TextView(mContext);
    mEndGroup = new RelativeLayout(mContext);
    mRowEndBG = new ImageView(mContext);
    mCheckbox = new CheckBox(mContext);
    mIsRowClickable = true;
  }
  
  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;

    // Create the views

    // Initialize the group for the frame
    mFrame.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    int padding = (int) (DENSITY * 1 + 0.5f);
    mFrame.setPadding(0, padding, 0, padding);
    mFrame.setBackgroundColor(R.color.black);

    // Initialize Layout that stores the contents
    mContents.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    mContents.setOrientation(LinearLayout.HORIZONTAL);
    mContents.setBackgroundColor(this.getResources().getColor(
        R.color.gameend_blankrow));

    // Initialize Pack Title
    mTitle.setText("Generic Pack Title");
    LinearLayout.LayoutParams titleTextParams = new LinearLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    titleTextParams.weight = 1.0f;
    mTitle.setLayoutParams(titleTextParams);
    mTitle.setPadding((int) (DENSITY * 10 + 0.5f), 0, 0, 0);
    mTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
    mTitle.setEllipsize(TruncateAt.END);
    mTitle.setHorizontallyScrolling(true);
    mTitle.setTextColor(this.getResources().getColor(R.color.white));
    // Make title clickable for info on the pack
    mTitle.setOnClickListener(mPackInfoRequestedListener);

    // Initialize End Group and add contents
    mRowEndBG.setImageResource(R.drawable.turnsum_row_end_white);
    
    // Initialize Price
    mPrice.setText("$1.99");
    RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    priceParams.addRule(ALIGN_PARENT_RIGHT);
    priceParams.addRule(CENTER_VERTICAL);
    priceParams.rightMargin=(int) (DENSITY * 8 + 0.5f);
    mPrice.setLayoutParams(priceParams);
    mPrice.setIncludeFontPadding(false);
    mPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

    RelativeLayout.LayoutParams checkboxParams = new RelativeLayout.LayoutParams(
        (int) (DENSITY * 42 + 0.5f), LayoutParams.WRAP_CONTENT);
    checkboxParams.addRule(ALIGN_PARENT_RIGHT);
    checkboxParams.addRule(CENTER_VERTICAL);
    checkboxParams.rightMargin=(int) (DENSITY * 13 + 0.5f);
    mCheckbox.setButtonDrawable(R.drawable.checkbox_packpurchase);
    mCheckbox.setLayoutParams(checkboxParams);
    mCheckbox.setClickable(false);
    mCheckbox.setFocusable(false);
    mCheckbox.setChecked(true);
    
    // Set fonts - Wrap in isInEditMode so as not to break previewer
    if(!this.isInEditMode())
    { 
      Typeface antonFont =
          Typeface.createFromAsset(mContext.getAssets(), "fonts/Anton.ttf");
      mPrice.setTypeface(antonFont);
      mTitle.setTypeface(antonFont);
    }

    RelativeLayout.LayoutParams mEndGroupParams = new RelativeLayout.LayoutParams(
        (int) (DENSITY * 109 + 0.5f), LayoutParams.WRAP_CONTENT);
    mEndGroup.setLayoutParams(mEndGroupParams);
    mEndGroup.addView(mRowEndBG);
    mEndGroup.addView(mPrice);
    mEndGroup.addView(mCheckbox);
    // Allow end piece to be clickable to select the pack
    mEndGroup.setOnClickListener(mSelectPackListener);

    // Add views to the contents layout
    mContents.addView(mTitle);
    mContents.addView(mEndGroup);

    // Add the views to frame
    mFrame.addView(mContents);

    // Enable clicking on the row by default
    setRowClickable(true);
    
    // Add groups to TeamSelectLayout
    this.addView(mFrame);

  }

  /**
   * Set the pack this Layout is associated with
   * 
   * @param pack
   *          The pack this Layout represents
   * @param isSelected
   *          Specify whether the pack is selected for the game
   */
  public void setPack(Pack pack, Boolean isSelected, Boolean isRowOdd) {
    mPack = pack;
    mIsPackEnabled = isSelected;
    mIsRowOdd = isRowOdd;
    refresh();
  }
  
  /**
   * Set the status of a layout while keeping pack the same
   * @param isSelected
   *          Specify whether the pack is selected for the game
   */
  public void setPackStatus(Boolean isSelected) {
    mIsPackEnabled = isSelected;
    refresh();
  }
  
  /*
   * Assign a listener to receive the OnPackSelected callback
   * @param listener Listener to receive the callback
   */
  public void setOnPackSelectedListener(OnPackSelectedListener listener) {
    mPackSelectedListener = listener;
  }

  /*
   * Assign a listener to receive the OnPackInfoRequested callback
   * @param listener Listener to receive the callback
   */
  public void setOnPackInfoRequestedListener(OnPackInfoRequestedListener listener) {
    mPackInfoListener = listener;
  }
  
  /**
   * Get the pack that is associated with this layout
   */
  public Pack getPack()
  {
    return mPack;
  }
  
  /**
   * Get whether or not this row is considered "odd"
   */
  public Boolean isRowOdd()
  {
    return mIsRowOdd;
  }
  
  /*
   * Set this row as unselectable, when the invoking classes don't need
   * selection events
   */
  public void setRowClickable(boolean isClickable)
  {
    mIsRowClickable = isClickable;
    mEndGroup.setClickable(mIsRowClickable);
    mTitle.setClickable(mIsRowClickable);
    if(isClickable)
    {
      mCheckbox.setVisibility(View.VISIBLE);
    }
    else
    {
      mCheckbox.setVisibility(View.INVISIBLE);
    }
  }
  
  /**
   * Refresh the view for a given row. This updates elements to represent
   * the corresponding pack.
   */
  public void refresh()
  {
    mTitle.setText(mPack.getName());
    int rowEndColor;
    if (mPack.isInstalled()) {
      if (mIsPackEnabled) {
        mCheckbox.setChecked(true);
        rowEndColor = R.color.packPurchaseSelected;
      } else {
        mCheckbox.setChecked(false);
        rowEndColor = R.color.packPurchaseUnSelected;
      }
      mRowEndBG.setColorFilter(this.getResources().getColor(rowEndColor),
          Mode.MULTIPLY);
      mPrice.setVisibility(View.INVISIBLE);

    } else {
      mPrice.setVisibility(View.VISIBLE);
      mCheckbox.setVisibility(View.INVISIBLE);
      mRowEndBG.setColorFilter(
          this.getResources().getColor(R.color.genericBG_trim), Mode.MULTIPLY);
    }
    int bgColor;
    // Set background
    if(mIsRowOdd)
    {
      bgColor = R.color.genericBG_trim;
    }
    else
    {
      bgColor = R.color.genericBG_trimDark;
    }
    mContents.setBackgroundColor(this.getResources().getColor(
        bgColor));
    
  }
  
  /**
   * Watches the group that selects and deselects the pack on click
   */
  private final OnClickListener mSelectPackListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "SelectPackListener onClick()");
      }
      Boolean newSelectionStatus = !mIsPackEnabled;
      setPackStatus(newSelectionStatus);

      // Send event to any listeners
      if (mPackSelectedListener != null) {
        mPackSelectedListener.onPackSelected(mPack, newSelectionStatus);
      }
    }
  };
  
  
  /**
   * Watches the group that shows pack Info
   */
  private final OnClickListener mPackInfoRequestedListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "PackInfoRequestedListener onClick()");
      }
      // Send event to any listeners
      if (mPackInfoListener != null) {
        mPackInfoListener.onPackInfoRequested(mPack);
      }
    }
  };

}
