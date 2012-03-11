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
import android.util.Log;

public class TickStateMachine {
  /**
   * 
   */
  public enum TickStates {
    UNSET, NORMAL, FAST, FASTER, FASTEST, PAUSED 
  }
  private TickStates mTickState;
  private TickStates mResumeToState;
  private int mTimerSoundId;
  private Context mContext;
  
  public TickStateMachine(Context context)
  {
    mTickState = TickStates.UNSET;
    mContext = context;
    mTimerSoundId = -1;
  }
  
  public TickStates getTickState()
  {
    return mTickState;
  }
  
  public void goToState(TickStates state)
  {
    mTickState = state;
    
    // Stop previous sound
    SoundManager sm = SoundManager.getInstance(mContext);
    if (mTimerSoundId > 0)
    {
      sm.stopSound(mTimerSoundId);
      mTimerSoundId = -1;
    }
    // Start next sound
    switch(state)
    {
      case NORMAL:
        Log.d("StateMachine", "GoTo Normal");
        mTimerSoundId = sm.playLoop(SoundManager.Sound.TICK_NORMAL);
        break;
      case FAST:
        Log.d("StateMachine", "GoTo Fast");
        mTimerSoundId = sm.playLoop(SoundManager.Sound.TICK_FAST);
        break;
      case FASTER:
        Log.d("StateMachine", "GoTo Faster");
        mTimerSoundId = sm.playLoop(SoundManager.Sound.TICK_FASTER);
        break;
      case FASTEST:
        Log.d("StateMachine", "GoTo Fastest");
        mTimerSoundId = sm.playLoop(SoundManager.Sound.TICK_FASTEST);
        break;
    }
  }
  
  public void pause()
  {
    mResumeToState = mTickState;
    mTickState = TickStates.PAUSED;
    SoundManager sm = SoundManager.getInstance(mContext);
    sm.stopSound(mTimerSoundId);
    mTimerSoundId = -1;
  }
  
  public void resume()
  {
    goToState(mResumeToState);
  }
}
