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
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
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
public class PackPurchaseRowLayout extends FrameLayout {

  protected static final String TAG = "PackPurchaseRowLayout";

  private Context mContext;

  // View members
  private RelativeLayout mContents;
  private ImageView mIcon;
  private TextView mTitle;
  private TextView mPrice;
  private ImageView mRowEndBG;
  
  // Data members
  private Pack mPack;
  private boolean mIsPackEnabled;
  private boolean mIsRowOdd;
  private boolean mIsPackPurchased;

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

  private void initializeMembers(Context context) {
    int id = 0;
    mContext = context;
    mContents = new RelativeLayout(mContext);
    mIcon = new ImageView(mContext);
    mIcon.setId(++id);
    mTitle = new TextView(mContext);
    mTitle.setId(++id);
    mPrice = new TextView(mContext);
    mPrice.setId(++id);
    mRowEndBG = new ImageView(mContext);
    mRowEndBG.setId(++id);
    mIsRowClickable = true;
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;

    // Create the views

    // Initialize the group for the frame
    this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    int padding = (int) (DENSITY * 1 + 0.5f);
    this.setPadding(0, padding, 0, padding);
    this.setBackgroundColor(R.color.black);

    // Initialize Layout that stores the contents
    mContents.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    mContents.setBackgroundColor(this.getResources().getColor(
        R.color.gameend_blankrow));
    
    // Add a placeholder for the icon
    mIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.pack0_icon));
    RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
    iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
    iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    mIcon.setLayoutParams(iconParams);
    int iconPadding = (int) (DENSITY * 5 + 0.5f);
    mIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);

    // Initialize Pack Title
    mTitle.setText("Generic Pack Title");
    RelativeLayout.LayoutParams titleTextParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    titleTextParams.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
    titleTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
    mTitle.setLayoutParams(titleTextParams);
    mTitle.setPadding((int) (DENSITY * 5 + 0.5f), 0, 0, 0);
    mTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
    mTitle.setWidth((int) (DENSITY * 190 + 0.5f));
    mTitle.setEllipsize(TruncateAt.END);
    mTitle.setHorizontallyScrolling(true);
    mTitle.setTextColor(this.getResources().getColor(R.color.text_default));

    // Initialize End Group and add contents
    mRowEndBG.setImageResource(R.drawable.turnsum_row_end_white);
    RelativeLayout.LayoutParams rowEndParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    rowEndParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    mRowEndBG.setLayoutParams(rowEndParams);
    // Old values: 109, wrap_content

    // Initialize Price
    mPrice.setText("$1.99");
    RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    priceParams.addRule(RelativeLayout.ALIGN_RIGHT, mRowEndBG.getId());
    priceParams.addRule(RelativeLayout.CENTER_VERTICAL);
    priceParams.rightMargin = (int) (DENSITY * 6 + 0.5f);
    mPrice.setLayoutParams(priceParams);
    mPrice.setIncludeFontPadding(false);
    mPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
    mPrice.setTextColor(this.getResources().getColor(R.color.text_default));

    // Set fonts - Wrap in isInEditMode so as not to break previewer
    if (!this.isInEditMode()) {
      Typeface antonFont = Typeface.createFromAsset(mContext.getAssets(),
          "fonts/Anton.ttf");
      mPrice.setTypeface(antonFont);
      mTitle.setTypeface(antonFont);
    }

    // Add views to the contents layout
    mContents.addView(mIcon);
    mContents.addView(mTitle);
    mContents.addView(mRowEndBG);
    mContents.addView(mPrice);

    // Add the views to frame
    this.addView(mContents);
    
    // Add single pixel bar of lightened color to give depth
    View lightBar = new View(mContext);
    lightBar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        (int) (DENSITY * 1 + 0.5f)));
    lightBar.setBackgroundColor(getResources().getColor(R.color.white));
    AlphaAnimation alpha = new AlphaAnimation(0.2f, 0.2f);
    alpha.setFillAfter(true);
    lightBar.startAnimation(alpha);
    this.addView(lightBar);
 
    // Enable clicking on the row by default
    setRowClickable(true);
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
    // Setup new members
    mPack = pack;
    mIsPackEnabled = isSelected;
    mIsRowOdd = isRowOdd;
    mIsPackPurchased = mPack.isInstalled();
    
    // Assign click listeners based on the pack's purchase state
    if(mIsPackPurchased)
    {
      mContents.setOnClickListener(mSelectPackListener);
    }
    else
    {
      mContents.setOnClickListener(mPackInfoRequestedListener);
    }  
    
    refresh();
  }

  /**
   * Set the status of a layout while keeping pack the same
   * 
   * @param isSelected
   *          Specify whether the pack is selected for the game
   */
  public void setPackStatus(Boolean isSelected) {
    mIsPackEnabled = isSelected;
    refresh();
  }

  /*
   * Assign a listener to receive the OnPackSelected callback
   * 
   * @param listener Listener to receive the callback
   */
  public void setOnPackSelectedListener(OnPackSelectedListener listener) {
    mPackSelectedListener = listener;
  }

  /*
   * Assign a listener to receive the OnPackInfoRequested callback
   * 
   * @param listener Listener to receive the callback
   */
  public void setOnPackInfoRequestedListener(
      OnPackInfoRequestedListener listener) {
    mPackInfoListener = listener;
  }

  /**
   * Get the pack that is associated with this layout
   */
  public Pack getPack() {
    return mPack;
  }

  /**
   * Get whether or not this row is considered "odd"
   */
  public Boolean isRowOdd() {
    return mIsRowOdd;
  }

  /*
   * Set this row as unselectable, when the invoking classes don't need
   * selection events
   */
  public void setRowClickable(boolean isClickable) {
    mIsRowClickable = isClickable;
    mContents.setClickable(mIsRowClickable);
    // Need to set PackInfo Bar to something generic without affecting
    // other uses
    // int bgColor = R.color.packPurchaseSelected;
    // mContents.setBackgroundColor(this.getResources().getColor(bgColor));
  }

  /**
   * Refresh the view for a given row. This updates elements to represent the
   * corresponding pack.
   */
  public void refresh() {
    
    // Set attributes that don't care about the state of the pack
    mTitle.setText(mPack.getName());
    mIcon.setImageDrawable(this.getResources().getDrawable(mPack.getIconID()));
    
    int bgColor;
    if (mIsPackPurchased) {
      if (mIsPackEnabled) {
        bgColor = R.color.packPurchaseSelected;
        mTitle.setTextColor(this.getResources().getColor(R.color.white));
        mIcon.setColorFilter(null);
      } else {
        bgColor = R.color.packPurchaseUnSelected2;
        mTitle.setTextColor(this.getResources()
            .getColor(R.color.genericBG_trim));
        mIcon.setColorFilter(this.getResources().getColor(R.color.genericBG_trimDark), Mode.MULTIPLY);
      }
      // Set background
      mContents.setBackgroundColor(this.getResources().getColor(bgColor));
      mPrice.setVisibility(View.INVISIBLE);
      mRowEndBG.setVisibility(View.INVISIBLE);
    } else {
      mPrice.setVisibility(View.VISIBLE);
      mRowEndBG.setVisibility(View.VISIBLE);
      mRowEndBG.setColorFilter(
          this.getResources().getColor(R.color.genericBG_trim), Mode.MULTIPLY);
      // Set background
      if (mIsRowOdd) {
        bgColor = R.color.genericBG_trim;
      } else {
        bgColor = R.color.genericBG_trimDark;
      }
      mContents.setBackgroundColor(this.getResources().getColor(bgColor));
    }

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
