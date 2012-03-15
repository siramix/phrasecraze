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
      sm.stopTick(mTimerSoundId);
      mTimerSoundId = -1;
    }
    // Start next sound
    switch(state)
    {
      case NORMAL:
        mTimerSoundId = sm.playTickLooped(SoundManager.Ticks.TICK_NORMAL);
        break;
      case FAST:
        mTimerSoundId = sm.playTickLooped(SoundManager.Ticks.TICK_FAST);
        break;
      case FASTER:
        mTimerSoundId = sm.playTickLooped(SoundManager.Ticks.TICK_FASTER);
        break;
      case FASTEST:
        mTimerSoundId = sm.playTickLooped(SoundManager.Ticks.TICK_FASTEST);
        break;
    }
  }
  
  public void pause()
  {
    // Don't try to repause if paused
    if( mTickState != TickStates.PAUSED)
    {
      Log.d("StateMachinnnnnnnnnnnnnnne", "GoTo PAUSED");
      mResumeToState = mTickState;
      mTickState = TickStates.PAUSED;
      SoundManager sm = SoundManager.getInstance(mContext);
      sm.stopTick(mTimerSoundId);
      mTimerSoundId = -1;
    }
  }
  
  public void resume()
  {
    goToState(mResumeToState);
  }
}
