package com.siramix.phrasecraze;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class PhrasePackPurchase extends Activity {

  private static final String TAG = "CardPackPurchase";

  private List<String> mPackList;
  private List<ImageView> mPackViewList;
  private List<View> mPackLineList;
  
  /**
   * Create the packages screen from an XML layout and
   * 
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "onCreate()");
    }
    
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.turnsummary);
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    GameManager game = application.getGameManager();
/*
    // Populate and display list of cards
    ScrollView list = (ScrollView) findViewById(R.id.PackPurchase_PackList);
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    // Iterate through all completed cards and set layout accordingly
    mPackViewList = new LinkedList<ImageView>();
    mPackLineList = new LinkedList<View>();
    //TODO get this list from the database or marketplace if possible 
    mPackList.add("Test1");
    mPackList.add("Test2");
    
    String packname;
    int count = 0;

    for (Iterator<String> it = mPackList.iterator(); it.hasNext();) {
      packname = it.next();

      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.packpurchaserow, layout);
      RelativeLayout realLine = (RelativeLayout) line.getChildAt(count);
      // Make every line alternating color
      if (count % 2 == 0) {
        View background = (View) realLine.getChildAt(0);
        background.setBackgroundResource(R.color.genericBG_trim);
      }
    }
  */}
}
