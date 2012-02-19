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

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.util.Log;

/**
 * This handles a single turn consisting of cards presented to a player for a
 * limited amount of time.
 * 
 * @author Siramix Labs
 */
public class Turn extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Turn";

  static final int DIALOG_PAUSED_ID = 0;
  static final int DIALOG_GAMEOVER_ID = 1;
  static final int DIALOG_READY_ID = 2;
  static final int DIALOG_ENDROUND_ID = 3;

  static final int TIMERANIM_PAUSE_ID = 0;
  static final int TIMERANIM_RESUME_ID = 1;
  static final int TIMERANIM_START_ID = 2;

  // Gesture thresholds expressed in dp
  private static final int SWIPE_MIN_DISTANCE_DP = 80;
  private static final int SWIPE_THRESHOLD_VELOCITY_DP = 150;
  // Convert DP thresholds to pixels for this phone (add .5f to round to nearest
  // pixel)
  private int mGestureThreshold;
  private int mGestureVelocityThreshold;

  private View mPauseOverlay;
  private ImageButton mCorrectButton;
  private ImageButton mSkipButton;
  private TextView mCountdownText;
  private TextView mTimesUpText;
  private LinearLayout mPauseTextLayout;
  private ViewFlipper mViewFlipper;

  private ImageView mTimerfill;

  private TextView mCardTitle;
  private ImageView mCardStatus;

  private RelativeLayout mTimerGroup;
  private RelativeLayout mButtonGroup;

  /**
   * Tracks the current state of the Turn as a boolean. Set to true when time
   * has expired and activity is showing the user "Time's up!"
   */
  private boolean mTurnIsOver = false;
  /**
   * Track when the game has paused. This will prevents code from executing
   * pointlessly if already paused.
   */
  private boolean mIsPaused = true;

  /**
   * This is a reference to the current game manager
   */
  private GameManager mGameManager;

  /**
   * Boolean to track which views are currently active
   */
  private boolean mAIsActive;

  /**
   * Boolean for representing whether we've gone back or not
   */
  private boolean mIsBack;

  /**
   * Boolean representing whether music is enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mMusicEnabled;

  /**
   * Boolean representing whether gestures are enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mGesturesEnabled;
  
  /**
   * Boolean representing whether or not assisted scoring is enabled. Reduces calls
   * to getprefs
   */
  private boolean mAssistedScoringEnabled;

  /**
   * Boolean representing whether skip is enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mSkipEnabled;
  
  /**
   * Boolean representing whether correct is enabled. 
   */
  private boolean mCorrectEnabled;

  /**
   * Boolean representing whether the countdown ticking has already been
   * started.
   */
  private boolean mIsTicking = false;

  /**
   * The time in miliseconds left when the ticking music began.
   */

  /**
   * Unique IDs for Options menu
   */
  protected static final int MENU_ENDGAME = 0;
  protected static final int MENU_SCORE = 1;
  protected static final int MENU_RULES = 2;

  /**
   * Swipe left for skip, right for back, up for right, and down for wrong.
   */
  private SimpleOnGestureListener mSwipeListener = new SimpleOnGestureListener() {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      // Do not let them do swipes while paused or time's up!
      if (mIsPaused || mTurnIsOver) {
        return false;
      }

      // Detect swipes in order of least to most harmful to a game -- ie if
      // a "correct" swipe is confused as a "skip" its not as bad as a skip
      // being
      // interpreted as a correct or wrong
      if (mGesturesEnabled) {
        if (mSkipEnabled && e1.getX() - e2.getX() > mGestureThreshold
            && Math.abs(velocityX) > mGestureVelocityThreshold) {
          Turn.this.doSkip();
          return true;
        } else if (e2.getX() - e1.getX() > mGestureThreshold
            && Math.abs(velocityX) > mGestureVelocityThreshold) {
          Turn.this.doBack();
          return true;
        } else if (mCorrectEnabled && e1.getY() - e2.getY() > mGestureThreshold
            && Math.abs(velocityY) > mGestureVelocityThreshold) {
          Turn.this.doCorrect();
          return true;
        }
      }

      return false;
    }
  };

  private GestureDetector mSwipeDetector;

  private OnTouchListener mGestureListener;

  private PauseTimer mCounter;
  private PauseTimer mResultsDelay;

  /**
   * Starts the turn timer and the animations that go along with that
   */
  private void startTimer() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "startTimer()");
    }

    mCounter.start();
    mTimerfill.startAnimation(timerAnimation(Turn.TIMERANIM_START_ID));
  }

  /**
   * Stops the turn timer and the animations that go along with that
   */
  private void stopTurnTimer() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "stopTimer()");
    }
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, Long.toString(mCounter.getTimeRemaining()));
    }
    if (!mTurnIsOver && mCounter.isActive()) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Do the Pause.");
      }
      mCounter.pause();
      mTimerfill.startAnimation(timerAnimation(Turn.TIMERANIM_PAUSE_ID));
    }
  }

  /**
   * Resumes the turn timer and the animations that go along with that
   */
  private void resumeTurnTimer() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "resumeTimer()");
    }
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, Long.toString(mCounter.getTimeRemaining()));
    }
    if (!mTurnIsOver && !mCounter.isActive()) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "Do the Resume.");
      }
      mCounter.resume();
      mTimerfill.startAnimation(timerAnimation(Turn.TIMERANIM_RESUME_ID));
    }
  }

  /**
   * Creates the menu items for the options menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreateOptionsMenu()");
    }

    menu.add(0, R.string.menu_EndRound, 0, "Forfeit Round");
    menu.add(0, R.string.menu_EndGame, 0, "End Game");
    menu.add(0, R.string.menu_Rules, 0, "Rules");

    return true;
  }

  /**
   * Handle various menu clicks
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onOptionsItemSelected()");
    }
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    // Handle item selection
    switch (item.getItemId()) {
    case R.string.menu_EndRound:
      // Play confirmation sound
	  sm.playSound(SoundManager.Sound.CONFIRM);
	  this.showDialog(DIALOG_ENDROUND_ID);
	  return true;
    case R.string.menu_EndGame:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      this.showDialog(DIALOG_GAMEOVER_ID);
      return true;
    case R.string.menu_Rules:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      startActivity(new Intent(getString(R.string.IntentRules), getIntent()
          .getData()));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Listener for click on the timer to pause
   */
  private final OnClickListener mTimerClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "TimerClickListener OnClick()");
      }
      Turn.this.pauseGame();
    }
  };

  /**
   * Listener for the 'Correct' button. It deals with the flip to the next card.
   */
  private final OnClickListener mCorrectListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "CorrectListener OnClick()");
      }

      Turn.this.doCorrect();
    }
  }; // End CorrectListener

  /**
   * Listener for the 'Skip' button. This deals with moving to the next card via
   * the ViewFlipper, but denotes that the card was skipped;
   */
  private final OnClickListener mSkipListener = new OnClickListener() {
    public void onClick(View v) {

      Turn.this.doSkip();
    }
  }; // End SkipListener

  /**
   * Listener for the pause overlay. It resumes the game.
   */
  private final OnClickListener mPauseListener = new OnClickListener() {
    public void onClick(View v) {
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "PauseListener OnClick()");
      }

      // If music is disabled, just resume the game immediately (don't wait for
      // music to seek unless it's begun)
      if (mTurnIsOver || (!mMusicEnabled && !mIsTicking)) {
        // Turn is over when timer reaches 0. At that point, we should just not
        // resume music
        Turn.this.resumeGame();
      } else if (mMusicEnabled || mIsTicking) {
        if (PhraseCrazeApplication.DEBUG) {
          Log.d(TAG, "unpause_OnClick ()");
        }
        // Resume must wait for music to seek back to the correct elapsed time
        PhraseCrazeApplication application = (PhraseCrazeApplication) Turn.this
            .getApplication();
        MediaPlayer mp = application.getMusicPlayer();
        int elapsedtime;
        if (mMusicEnabled) {
          elapsedtime = mGameManager.getTurnTime()
              - (int) mCounter.getTimeRemaining();
        } else {
          elapsedtime = 10000 - (int) mCounter.getTimeRemaining();
          if (PhraseCrazeApplication.DEBUG) {
            Log.d(TAG, "Resume ticking at " + elapsedtime);
          }
        }
        // Return to the elapsed time
        mp.seekTo(elapsedtime);
        mp.setOnSeekCompleteListener(new TurnMusicListener());
      }

      // Hide overlays here so that they can't report multiple OnClick'ed while
      // music seeks
      mPauseOverlay.setVisibility(View.INVISIBLE);
      mPauseTextLayout.setVisibility(View.INVISIBLE);
    }
  }; // End CorrectListener

  /**
   * Returns an animation that brings cards into view from the right of the
   * screen
   * 
   * @return Animation that brings cards into view from the right of the screen
   */
  private Animation inFromRightAnimation() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "InFromRightAnimation()");
    }
    Animation inFromRight = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    inFromRight.setDuration(500);
    return inFromRight;
  }

  /**
   * Returns an animation that tosses the cards from the view out to the left
   * 
   * @return Animation that tosses the cards from the view out to the left
   */
  private Animation outToLeftAnimation() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "OutToLeftAnimation()");
    }
    Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
        0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    outToLeft.setDuration(500);
    return outToLeft;
  }

  /**
   * Returns an animation that moves the card back in from the left (on back
   * state)
   * 
   * @return Animation that moves the card back in from the left
   */
  private Animation backInFromLeftAnimation() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "BackInFromLeftAnimation()");
    }
    Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
        -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    outToLeft.setDuration(500);
    return outToLeft;
  }

  /**
   * Returns an animation that moves the card out to the right (on back state)
   * 
   * @return Animation that moves the card out to the right
   */
  private Animation backOutToRightAnimation() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "BackOutToRightAnimation()");
    }
    Animation inFromRight = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    inFromRight.setDuration(500);
    return inFromRight;
  }

  /**
   * Returns an animation that slides the timer off the screen
   * 
   * @return Animation that slides the timer off screen
   */
  private Animation showTimerAnim(boolean show) {
    float y0 = 0.0f;
    float y1 = -1.0f;
    Animation slideUp;
    if (show) {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y1,
          Animation.RELATIVE_TO_SELF, y0);

      slideUp.setInterpolator(new DecelerateInterpolator());
    } else {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y0,
          Animation.RELATIVE_TO_SELF, y1);

      slideUp.setInterpolator(new AccelerateInterpolator());
    }
    slideUp.setDuration(250);
    // make the element maintain its orientation even after the animation
    // finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }

  /**
   * Returns an animation that slides the buttons on or off the screen
   * 
   * @return Animation that slides the buttons on or off screen
   */
  private Animation showButtonsAnim(boolean show) {
    float y0 = 0.0f;
    float y1 = 1.0f;
    Animation slideUp;
    if (show) {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y1,
          Animation.RELATIVE_TO_SELF, y0);

      slideUp.setInterpolator(new DecelerateInterpolator());
    } else {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y0,
          Animation.RELATIVE_TO_SELF, y1);

      slideUp.setInterpolator(new AccelerateInterpolator());
    }
    slideUp.setDuration(250);

    // make the element maintain its orientation even after the animation
    // finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }

  /**
   * Animation method for the timer bar that takes an integer to determine
   * whether it is starting, resuming, or stopping animation.
   * 
   * @param timerCommand
   *          an integer value of 0 for Pause, 1 for Resume, and 2 for Start
   * 
   * @return The animation that scales the timer as the time depletes
   */
  private Animation timerAnimation(int timerCommand) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "TimerAnimation()");
    }

    float percentTimeLeft = ((float) mCounter.getTimeRemaining() / mGameManager
        .getTurnTime());
    int duration = mGameManager.getTurnTime();

    if (timerCommand == Turn.TIMERANIM_RESUME_ID) {
      duration = (int) mCounter.getTimeRemaining();
    } else if (timerCommand == Turn.TIMERANIM_PAUSE_ID) {
      duration = Integer.MAX_VALUE;
    }

    ScaleAnimation scaleTimer = new ScaleAnimation(percentTimeLeft, 0.0f, 1.0f,
        1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
        1.0f);

    scaleTimer.setDuration(duration);
    scaleTimer.setInterpolator(new LinearInterpolator());
    return scaleTimer;
  }

  /**
   * Works with GameManager to perform the back end processing of a correct
   * card. For consistency this method was created to match the skip
   * architecture. Also for consistency the sound for correct cards will be
   * handled in this method.
   */
  protected void doCorrect() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "doCorrect()");
    }

    ViewFlipper flipper = (ViewFlipper) findViewById(R.id.Turn_ViewFlipper);

    mAIsActive = !mAIsActive;
    flipper.showNext();
    mGameManager.processCard(Card.RIGHT);

    
    // Mark the card with an icon
    mCardStatus.setBackgroundResource(Card.getCardMarkDrawableId(Card.RIGHT));
    mCardStatus.setVisibility(View.VISIBLE);

    // Only play sound once card has been processed so we don't confuse the user
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.RIGHT);

    // Show the next card
    showCard(false);
    
    mIsBack = false;
  }

  /**
   * Works with GameManager to perform the back end processing of a card skip.
   * Also handles the sound for skipping so that all forms of skips (swipes or
   * button clicks) play the sound.
   */
  protected void doSkip() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "doSkip()");
    }

    mAIsActive = !mAIsActive;
    mViewFlipper.showNext();
    mGameManager.processCard(Card.SKIP);

    // Mark the card with an icon for SKIP

    mCardStatus.setBackgroundResource(Card.getCardMarkDrawableId(Card.SKIP));
    mCardStatus.setVisibility(View.VISIBLE);
    
    // Only play sound once card has been processed so we don't confuse the user
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.SKIP);

    // Show the next card
    showCard(false);
    
    mIsBack = false;
  }

  /**
   * Handle when a back button is pressed (we only let the user go back one card
   * at this time.
   */
  protected void doBack() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "doBack()");
    }
    if (mIsBack) {
      return;
    }
    mIsBack = true;

    mAIsActive = !mAIsActive;

    // Reassign view to animate backwards
    mViewFlipper.setInAnimation(backInFromLeftAnimation());
    mViewFlipper.setOutAnimation(backOutToRightAnimation());

    mViewFlipper.showNext();

    // Restore animations for future actions
    mViewFlipper.setInAnimation(inFromRightAnimation());
    mViewFlipper.setOutAnimation(outToLeftAnimation());

    mGameManager.processBack();
    
    this.showCard(true);

    // Play back sound
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.BACK);
  }

  /**
   * Updates references to reflect the A or B status of the current card
   */
  protected void setActiveCard() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setActiveCard()");
    }
    int curTitle;
    int curStatus;
    if (mAIsActive) {
      curTitle = R.id.Turn_CardTitleA;
      curStatus = R.id.Turn_StatusImageA;
    } else {
      curTitle = R.id.Turn_CardTitleB;
      curStatus = R.id.Turn_StatusImageB;
    }

    mCardTitle = (TextView) this.findViewById(curTitle);
    mCardStatus = (ImageView) this.findViewById(curStatus);

  }

  /**
   * Function for changing the display to display the current Card.
   */
  protected void showCard(boolean showCardStatus) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "ShowCard()");
    }

    this.setActiveCard();

    Card curCard = mGameManager.getCurrentCard();
    mCardTitle.setText(curCard.getTitle());
    if(showCardStatus)
    	mCardStatus.setVisibility(View.VISIBLE);
    else
        // Hide the card status until marked
        mCardStatus.setVisibility(View.INVISIBLE);
    
    // Change team colors only in assisted scoring mode
    if(mAssistedScoringEnabled)
    {
      updateTeamColoring();
    }
  }
  
  /**
   * Helper function that colors elements based on the current team
   */
  protected void updateTeamColoring()
  {
    // Change views to appropriate team color
    Team curTeam = mGameManager.getActiveTeam();
    ImageView barFill = (ImageView) this.findViewById(R.id.Turn_TimerFill);
    barFill.setImageResource(curTeam.getPrimaryColor());
    this.findViewById(R.id.Turn_ViewFlipper).setBackgroundResource(
        curTeam.getGradient());    
  }
  
  /**
   * Helper function to color the scoreboard elements based
   * on the participating teams
   */
  protected void updateScoreboard()
  {
    List<Team> teams = mGameManager.getTeams();
    // Update scoreboard colors
    int[] scoreTextViews = {R.id.Turn_ScoreA, R.id.Turn_ScoreB};
    int[] scoreTextBGs = {R.id.Turn_ScoreABG, R.id.Turn_ScoreBBG};
    int[] scoreFrameLayouts = {R.id.Turn_ScoreA_Frame, R.id.Turn_ScoreB_Frame};
    for(int i = 0; i < scoreTextViews.length; i++)
    {
      TextView score = (TextView) this.findViewById(scoreTextViews[i]);
      score.setText(Integer.toString((teams.get(i).getScore())));
      View scoreBG = (View) this.findViewById(scoreTextBGs[i]);
      scoreBG.setBackgroundColor(this.getResources().getColor(teams.get(i).getPrimaryColor()));
      FrameLayout frame = (FrameLayout) this.findViewById(scoreFrameLayouts[i]);
      frame.setBackgroundColor(this.getResources().getColor(teams.get(i).getComplementaryColor()));
    }
  }

  /**
   * OnTimeExpired defines what happens when the player's turn timer runs out
   */
  protected void onTimeExpired() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onTimeExpired()");
    }
    mResultsDelay = new PauseTimer(1500) {
      @Override
      public void onFinish() {
        Turn.this.endTurn();
      }

      @Override
      public void onTick() {
        // Do nothing on tick
      }
    };
    mResultsDelay.start();

    // Hide timer bar and time
    ImageView timerFill = (ImageView) this.findViewById(R.id.Turn_TimerFill);
    timerFill.setVisibility(View.INVISIBLE);
    RelativeLayout timerGroup = (RelativeLayout) this
        .findViewById(R.id.Turn_TimerBar);
    timerGroup.startAnimation(this.showTimerAnim(false));
    // Hide buttons
    RelativeLayout buttonGroup = (RelativeLayout) this
        .findViewById(R.id.Turn_LowBar);
    buttonGroup.startAnimation(this.showButtonsAnim(false));

    // Only play gong if music is off
    if (!mMusicEnabled) {
      SoundManager sm = SoundManager.getInstance(this.getBaseContext());
      sm.playSound(SoundManager.Sound.GONG);
    }

    TextView timer = (TextView) this.findViewById(R.id.Turn_Timer);
    timer.setVisibility(View.INVISIBLE);

    // Hide card
    this.setActiveCard();
    mViewFlipper.setVisibility(View.INVISIBLE);

    // turn off buttons
    mSkipButton.setEnabled(false);
    mCorrectButton.setEnabled(false);

    TextView timesUpView = (TextView) this.findViewById(R.id.Turn_TimesUp);
    timesUpView.setVisibility(View.VISIBLE);
  }

  /**
   * Hands off the intent to the next turn summary activity.
   */
  protected void endTurn() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onTurnEnd()");
    }
    mGameManager.processCard(Card.WRONG);
    mGameManager.setBuzzedTeam(mGameManager.getActiveTeam());
    
    startActivity(new Intent(getString(R.string.IntentTurnSummary), getIntent()
        .getData()));
  }

  /**
   * Get references to all of the UI elements that we need to work with after
   * the activity creation
   */
  protected void setupViewReferences() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setupViewReferences()");
    }

    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    mGameManager = application.getGameManager();

    mPauseOverlay = (View) this.findViewById(R.id.Turn_PauseImageView);
    mCountdownText = (TextView) findViewById(R.id.Turn_Timer);
    mViewFlipper = (ViewFlipper) this.findViewById(R.id.Turn_ViewFlipper);
    mTimesUpText = (TextView) this.findViewById(R.id.Turn_TimesUp);

    mCorrectButton = (ImageButton) this.findViewById(R.id.Turn_ButtonCorrect);
    mSkipButton = (ImageButton) this.findViewById(R.id.Turn_ButtonSkip);

    mTimerfill = (ImageView) this.findViewById(R.id.Turn_TimerFill);
    mPauseTextLayout = (LinearLayout) this
        .findViewById(R.id.Turn_PauseTextGroup);

    mTimerGroup = (RelativeLayout) this.findViewById(R.id.Turn_TimerBar);
    mButtonGroup = (RelativeLayout) this.findViewById(R.id.Turn_LowBar);
  }

  /**
   * Set the initial properties for UI elements
   */
  protected void setupUIProperties() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setupUIProperties()");
    }
    mPauseOverlay.setVisibility(View.INVISIBLE);
    mPauseOverlay.setOnClickListener(mPauseListener);

    mTimerGroup.setOnClickListener(mTimerClickListener);

    mViewFlipper.setInAnimation(inFromRightAnimation());
    mViewFlipper.setOutAnimation(outToLeftAnimation());

    mCorrectButton.setOnClickListener(mCorrectListener);
    mSkipButton.setOnClickListener(mSkipListener);
 
    if(mCorrectEnabled)
    {
        // Set visibility and control of Skip Button
        if (mSkipEnabled) {
          mSkipButton.setVisibility(View.VISIBLE);
        } else {
          mSkipButton.setVisibility(View.GONE);
        }
        mCorrectButton.setVisibility(View.VISIBLE);
    }
    else
    {
    	mCorrectButton.setVisibility(View.GONE);
    }


    // Listen for all gestures
    mSwipeDetector = new GestureDetector(mSwipeListener);
    mGestureListener = new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (mSwipeDetector.onTouchEvent(event)) {
          return true;
        }
        return true; // prevents highlighting badwords by consuming even if
        // not detected as a swipe
      }
    };

    // Setup the "card" views to allow for skip gesture to be performed on top
    TextView titleA = (TextView) this.findViewById(R.id.Turn_CardTitleA);
    titleA.setOnTouchListener(mGestureListener);
    TextView titleB = (TextView) this.findViewById(R.id.Turn_CardTitleB);
    titleB.setOnTouchListener(mGestureListener);
    this.findViewById(R.id.Turn_Root).setOnTouchListener(mGestureListener);
    this.findViewById(R.id.Turn_ViewFlipper).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_CardLayoutA).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_CardLayoutB).setOnTouchListener(
        mGestureListener);

    Typeface font = Typeface.createFromAsset(getAssets(),
    "fonts/FrancoisOne.ttf");
    titleA.setTypeface(font);
    titleB.setTypeface(font);
    
    // Only change team color in assisted scoring mode  
    if( mAssistedScoringEnabled)
    {
      updateTeamColoring();
    }
    
    updateScoreboard();
  }

  /**
   * Setup the turn timer and return a reference to it
   * 
   * @return a reference to the turn timer
   */
  public PauseTimer setupTurnTimer() {
    // Initialize the turn timer
    long time = mGameManager.getTurnTime();
    return new PauseTimer(time) {
      @Override
      public void onFinish() {
        Turn.this.onTimeExpired();
        mCountdownText.setText("0");
        mTurnIsOver = true;
      }

      @Override
      public void onTick() {
        if (PhraseCrazeApplication.DEBUG_TIMERTICKS) {
          Log.d(TAG, Long.toString(mCounter.getTimeRemaining()));
        }
        // Update our text each second
        long shownTime = (mCounter.getTimeRemaining() / 1000) + 1;
        mCountdownText.setText(Long.toString(shownTime));

        // When music is not enabled, use the ticking sound
        if (!mMusicEnabled && !mIsTicking) {
          if (shownTime == 10) {
            if (PhraseCrazeApplication.DEBUG) {
              Log.d(TAG, "Queue tick 'music' ");
            }
            mIsTicking = true;
            PhraseCrazeApplication application = (PhraseCrazeApplication) Turn.this
                .getApplication();
            MediaPlayer mp = application.getMusicPlayer();
            mp.start();
          }
        }
      }

    };

  }

  /**
   * Initializes the activity to display the word you have to cause your team
   * mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }
    final float scale = Turn.this.getResources().getDisplayMetrics().density;
    mGestureThreshold = (int) (SWIPE_MIN_DISTANCE_DP * scale + 0.5f);
    mGestureVelocityThreshold = (int) (SWIPE_THRESHOLD_VELOCITY_DP * scale + 0.5f);
    
    // Set local variable for assisted scoring so that we don't have 
    // to make many calls to game manager
    PhraseCrazeApplication application = (PhraseCrazeApplication) Turn.this
        .getApplication();
    GameManager curGame = application.getGameManager();
    mAssistedScoringEnabled = curGame.isAssistedScoringEnabled();
    
    // Capture our preference variable for music, skip, and gestures once
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    if (sp.getBoolean("music_enabled", true))
      mMusicEnabled = true;
    else
      mMusicEnabled = false;

    // Set local variable for skip preference to reduce calls to get
    if (sp.getBoolean("allow_skip", true))
      mSkipEnabled = true;
    else
      mSkipEnabled = false;

    // Set local variable for allowing gesture preference to reduce get calls
    if (sp.getBoolean("allow_gestures", true))
      mGesturesEnabled = true;
    else
      mGesturesEnabled = false;

    mCorrectEnabled = mAssistedScoringEnabled;
    
    
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // set which card is active
    mAIsActive = true;
    mIsBack = true;

    // Setup the view
    this.setContentView(R.layout.turn);

    this.setupViewReferences();

    this.setupUIProperties();

    this.showDialog(DIALOG_READY_ID);

    this.mCounter = setupTurnTimer();

  }

  /**
   * Make sure to pause the game if the activity loses focus (enters the
   * "paused" state)
   */
  @Override
  public void onPause() {
    super.onPause();
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onPause()");
    }
    if (!mIsPaused && !mTurnIsOver) {
      this.pauseGame();
    }
  }

  /**
   * Display a dialog when the search is requested
   */
  @Override
  public boolean onSearchRequested() {
    if(PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onSearchRequested()");
    }
    return false;
  }

  /**
   * Create game over and ready dialogs using builders
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onCreateDialog(" + id + ")");
    }
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case DIALOG_GAMEOVER_ID:
      builder = new AlertDialog.Builder(this);
      builder.setMessage("Are you sure you want to end the current game?")
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(Turn.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);

              // Set the current team as the buzzed team and auto assign points
              // We auto assign points so that some score exists, though 
              // we could end in tie.
              mGameManager.setBuzzedTeam(mGameManager.getActiveTeam());
              mGameManager.setAutoAssignedRoundScores();
              mGameManager.endGame();
              
              startActivity(new Intent(getString(R.string.IntentEndGame),
                  getIntent().getData()));
            }
          }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(Turn.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.BACK);

              dialog.cancel();
            }
          });
      dialog = builder.create();
      break;
    case DIALOG_ENDROUND_ID:
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to forfeit the round?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // Play confirmation sound
                SoundManager sm = SoundManager.getInstance(Turn.this
                    .getBaseContext());
                sm.playSound(SoundManager.Sound.CONFIRM);

                Turn.this.endTurn();
              }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // Play confirmation sound
                SoundManager sm = SoundManager.getInstance(Turn.this
                    .getBaseContext());
                sm.playSound(SoundManager.Sound.BACK);

                dialog.cancel();
              }
            });
        dialog = builder.create();
        break;
    case DIALOG_READY_ID:
      // Play team ready sound
      SoundManager sm = SoundManager.getInstance(Turn.this.getBaseContext());
      sm.playSound(SoundManager.Sound.TEAMREADY);

      String readyPrompt;
      if( mAssistedScoringEnabled)
      {
        String curTeam = mGameManager.getActiveTeam().getName();
        readyPrompt = "Ready " + curTeam + "?";
      }
      else
      {
        readyPrompt = "Ready?";
      }
      
      builder = new AlertDialog.Builder(this);
      builder.setMessage(readyPrompt)
          .setCancelable(false)
          .setPositiveButton("Start!", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
              mIsPaused = false;
              mIsBack = true;
              dialog.dismiss();
              Turn.this.showCard(false);
              Turn.this.startTimer();

              // Play back sound to differentiate from normal clicks
              SoundManager sm = SoundManager.getInstance(Turn.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.BACK);

              // Start the turn music
              PhraseCrazeApplication application = (PhraseCrazeApplication) Turn.this
                  .getApplication();
              GameManager gm = application.getGameManager();

              int musicId = R.raw.mus_countdown;
              // If music is enabled, select the appropriate track
              if (mMusicEnabled) {
                switch (gm.getTurnTime()) {
                case 30000:
                  musicId = R.raw.mus_round_30;
                  break;
                case 60000:
                  musicId = R.raw.mus_round_60;
                  break;
                case 90000:
                  musicId = R.raw.mus_round_90;
                  break;
                }
              }

              MediaPlayer mp = application.createMusicPlayer(Turn.this
                  .getBaseContext(), musicId);
              // If music is not enabled, it will start the countdown track at
              // 10 seconds
              if (mMusicEnabled) {
                mp.start();
              }

            }
          });
      dialog = builder.create();

      // We add an onDismiss listener to handle the case in which a user attempts
      // to search on the ready dialog
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

        public void onDismiss(DialogInterface dialog) {
          if(mIsPaused) {
            showDialog(DIALOG_READY_ID);
          }
        }

      });
      break;
    default:
      dialog = null;
    }
    return dialog;

  }

  /**
   * Class tracks the seek time on the music realignment that happens on every
   * resume.
   */
  private class TurnMusicListener implements OnSeekCompleteListener {
    public void onSeekComplete(MediaPlayer mp) {

      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "onSeekComplete");
      }
      // Resume the game on seek complete
      Turn.this.resumeGame();

      // Resume the music
      if (mMusicEnabled || (!mMusicEnabled && mIsTicking)) {
        mp.start();
      }
    }
  }

  /**
   * Resume the game by showing/enabling the proper view elements and resuming
   * the turn timer.
   */
  protected void resumeGame() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "resumeGameTurn()");
    }
    mIsPaused = false;

    if (!mTurnIsOver) {
      this.resumeTurnTimer();

      this.setActiveCard();

      // Play resume sound
      SoundManager sm = SoundManager.getInstance(this.getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);

      mViewFlipper.setVisibility(View.VISIBLE);

      mSkipButton.setEnabled(true);
      mCorrectButton.setEnabled(true);

      mTimerGroup.startAnimation(this.showTimerAnim(true));
      mButtonGroup.startAnimation(this.showButtonsAnim(true));
    } else {
      mResultsDelay.resume();

      // Show TimesUp text when resuming after time has expired
      mTimesUpText.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Perform the logic of hiding card elements and stopping the turn timer when
   * we pause the game through whatever pausing method.
   */
  protected void pauseGame() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "pauseGameTurn()");
    }
    mIsPaused = true;
    mPauseOverlay.setVisibility(View.VISIBLE);

    mPauseTextLayout = (LinearLayout) this
        .findViewById(R.id.Turn_PauseTextGroup);
    mPauseTextLayout.setVisibility(View.VISIBLE);

    // Stop music
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    if (mp.isPlaying()) {
      mp.pause();
    }

    // Play ready sound since it indicates a wait.
    // This is the menu method that is called on every menu push
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.TEAMREADY);

    if (!mTurnIsOver) {
      this.stopTurnTimer();

      this.setActiveCard();

      mViewFlipper.setVisibility(View.INVISIBLE);
      mSkipButton.setEnabled(false);
      mCorrectButton.setEnabled(false);

      mTimerGroup.startAnimation(this.showTimerAnim(false));
      mButtonGroup.startAnimation(this.showButtonsAnim(false));
    } else {
      mResultsDelay.pause();

      // Hide TimesUp text when pause after time has expired
      mTimesUpText.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * We make sure to pause the game when the menu is opened
   */
  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    if (!mIsPaused) {
      this.pauseGame();
    }

    return true;
  }

  /**
   * Handler for key down events. This will start tracking the back button event
   * so we can properly catch it and move back between cards instead of
   * activities
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onKeyDown()");
    }

    // Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      event.startTracking();
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Handle the back button such that we go back between cards instead of to the
   * previous activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "onKeyUp()");
    }

    // Back button should go to the previous card
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      if (!(mIsPaused || mTurnIsOver)) {
        this.doBack();
      }
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Consume touch events to prevent anything other than the swipe detector from
   * operating
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mSwipeDetector.onTouchEvent(event))
      return true;
    else
      return false;
  }

}
