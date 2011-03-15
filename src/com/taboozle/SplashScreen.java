package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

/**
 * This is the splash screen for the app's intro.  It should be started before the title
 * and should actually start the music.
 * 
 * @author Taboozle Team
 *
 */
public class SplashScreen extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "SplashScreen";
  
  protected int SPLASHTIME = 3000; //length of time splash screen will show.
  protected Handler exitHandler = null; //will be used to delay the run of splash's exit
  protected Runnable exitRunnable = null; //will be called when splash is ready to close
  
  /**
   * called on creation of splash screen activity
   */
  public void onCreate(Bundle savedInstanceState)
  {
    Log.d( TAG, "onCreate()" );
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splashscreen);
    
    // Runnable exiting the splash screen and launching the menu
    exitRunnable = new Runnable() 
    {
      public void run()
      {
        exitSplash();
      }
    };
    
    // Run the exitRunnable in in SPLASHTIME ms
    exitHandler = new Handler();
    exitHandler.postDelayed(exitRunnable, SPLASHTIME);
  }
  
  /**
   * onTouchEvent to handle interrupts to splash screen
   */
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    Log.d( TAG, "onTouchEvent()" );
    
    if (event.getAction() == MotionEvent.ACTION_DOWN)
    {
      // Remove the exitRunnable callback from the handler queue
      exitHandler.removeCallbacks(exitRunnable);
      
      // Run the exit code manually
      exitSplash();
    }
    
    return true;
  }
  
  /**
   * called when exithandler is reached or a touch event occurs
   */
  private void exitSplash()
  {
    Log.d( TAG, "exitSplash()" );
    
    finish();
    startActivity(new Intent("com.taboozle.intent.action.TITLE"));
  }
}
