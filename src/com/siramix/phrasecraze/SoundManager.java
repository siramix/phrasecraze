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
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundManager {

  private static SoundManager mInstance;
  private static AudioManager mAudioManager;
  private static Context mContext;
  private static SoundPool mSoundPool;
  private static SoundPool mTickSoundPool;

  public static enum Sound {
    RIGHT, SKIP, TEAMREADY, WIN, BACK, CONFIRM, TIMEUP
  };
  
  public static enum Ticks {
    TICK_NORMAL, TICK_FAST, TICK_FASTER, TICK_FASTEST
  };

  /**
   * Logging tag
   */
  public static String TAG = "SoundManager";

  /**
   * Array for storing system sound ids for the sounds loaded into the pool
   */
  private static int[] mSoundIds;
  
  /**
   * Array for storing tick sound ids
   */
  private static int[] mTickIds;

  /**
   * Default constructor
   */
  public SoundManager(Context baseContext) {
    initSoundManager(baseContext);
    loadSounds();
  }

  /**
   * Create or get the single instance to SoundManager
   * 
   * @param baseContext
   * @return
   */
  public synchronized static SoundManager getInstance(Context baseContext) {
    if (mInstance == null) {
      mInstance = new SoundManager(baseContext);
    }
    return mInstance;
  }

  /**
   * Create any objects and references needed for a SoundManager
   * 
   * @param baseContext
   */
  private void initSoundManager(Context baseContext) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "initSoundManager()");
    }
    mContext = baseContext;
    mAudioManager = (AudioManager) mContext
        .getSystemService(Context.AUDIO_SERVICE);
    mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
    mTickSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
  }

  /**
   * Load all the sounds for the game
   */
  private void loadSounds() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "loadSounds()");
    }
    mSoundIds = new int[Sound.values().length];
    mSoundIds[Sound.RIGHT.ordinal()] = mSoundPool.load(mContext,
        R.raw.fx_right, 1);
    mSoundIds[Sound.SKIP.ordinal()] = mSoundPool.load(mContext, R.raw.fx_skip,
        1);
    mSoundIds[Sound.TEAMREADY.ordinal()] = mSoundPool.load(mContext,
        R.raw.fx_teamready, 1);
    mSoundIds[Sound.WIN.ordinal()] = mSoundPool.load(mContext, R.raw.fx_win, 1);
    mSoundIds[Sound.BACK.ordinal()] = mSoundPool.load(mContext, R.raw.fx_back,
        1);
    mSoundIds[Sound.CONFIRM.ordinal()] = mSoundPool.load(mContext,
        R.raw.fx_confirm, 1);
    mSoundIds[Sound.TIMEUP.ordinal()] = mSoundPool.load(mContext,
        R.raw.fx_timeup, 1);
    
    mTickIds = new int[Ticks.values().length];
    mTickIds[Ticks.TICK_NORMAL.ordinal()] = mTickSoundPool.load(mContext,
        R.raw.fx_tick_normal, 1);
    mTickIds[Ticks.TICK_FAST.ordinal()] = mTickSoundPool.load(mContext,
        R.raw.fx_tick_fast, 1);
    mTickIds[Ticks.TICK_FASTER.ordinal()] = mTickSoundPool.load(mContext,
        R.raw.fx_tick_faster, 1);
    mTickIds[Ticks.TICK_FASTEST.ordinal()] = mTickSoundPool.load(mContext,
        R.raw.fx_tick_fastest, 1);
  }

  /**
   * Play a sound once
   * 
   * @param fxIndex
   *          the sound to be played (once)
   * @return the id of the sound in the sound pool
   */
  public int playSound(Sound fxIndex) {

    // Volume% = current volume / max volume
    float volume = (float) mAudioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC)
        / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    return mSoundPool.play(mSoundIds[fxIndex.ordinal()], volume, volume, 1, 0,
        1.0f);
  }

  /**
   * Plays a sound looped
   * 
   * @param fxIndex
   *          the sound to be played FOREVER
   * @return the id of the sound in the sound pool
   */
  public int playLoop(Sound fxIndex) {
    // Volume% = current volume / max volume
    float volume = (float) mAudioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC)
        / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    return mSoundPool.play(mSoundIds[fxIndex.ordinal()], volume, volume, 1, -1,
        1.0f);
  }

  /**
   * Stop sound playing with the id listed
   * 
   * @param soundId
   */
  public void stopSound(int soundId) {
    mSoundPool.stop(soundId);
  }
  
  /*
   * Plays the specified Tick sound effect looped on the TickSoundPool.
   * @param tick - Tick sound effect to play
   */
  public int playTickLooped(Ticks tick)
  {
    // Volume% = current volume / max volume
    float volume = (float) mAudioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC)
        / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    return mTickSoundPool.play(mTickIds[tick.ordinal()], volume, volume, 1, -1,
        1.0f);
  }

  /*
   * Stops specified Tick sound effect on the TickSoundPool
   * @param soundId - tick sound effect ID to stop
   */
  public void stopTick(int soundId) {
    mTickSoundPool.stop(soundId);
  }
}
