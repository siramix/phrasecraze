package com.siramix.phrasecraze;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhrasePackPurchase extends Activity {

  private static final String TAG = "CardPackPurchase";
  
  private List<String> mPackList;  // This must stay in sync with mPackLineList
  List<ImageView> mPackViewList;
  List<View> mPackLineList;
  
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
    this.setContentView(R.layout.packpurchase);
    
    // Get our current context
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    GameManager game = application.getGameManager();

    // set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");
    TextView header = (TextView) this.findViewById(R.id.PackPurchase_Title);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_FreePackTitle);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_PaidPackTitle);
    header.setTypeface(antonFont);
    
    // Populate and display list of cards
    LinearLayout freePackLayout = (LinearLayout) findViewById(R.id.PackPurchase_FreePackSets);
    LinearLayout paidPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_PaidPackSets);
    
    // Instantiate all of our lists for programmatic adding of packs to view
    mPackViewList = new LinkedList<ImageView>();
    mPackLineList = new LinkedList<View>();
    mPackList = new LinkedList<String>(); // TODO This s/b a master list of packs
        
    //TODO pack list should be filled with all our packs
    
    //TODO get this list from the database or marketplace if possible
    //      this is the free pack list
    LinkedList<String> freePackList = new LinkedList<String>();
    freePackList.add("Test1");
    freePackList.add("Test2");
    freePackList.add("Test3");
    
    //TODO get this list from the database or marketplace if possible
    //      this is the paid pack list
    LinkedList<String> paidPackList = new LinkedList<String>();
    paidPackList.add("PaidTest1");
    paidPackList.add("PaidTest2");
    paidPackList.add("PaidTest3");
    paidPackList.add("PaidTest4");
    paidPackList.add("PaidTest5");
    paidPackList.add("PaidTest6");
    
    populatePackLayout(freePackList, freePackLayout);
    populatePackLayout(paidPackList, paidPackLayout);
  }
  
  /**
   * Create dynamic rows of packs at runtime for pack purchase view.  This will
   * set up the XML, bind listeners, and update pack titles, price, and images according to the
   * information passed in.  Retrieving the info to populate should be the responsibility
   * of a different method.
   * 
   * @param packlist  A list of packs to iterate through and populate the purchase rows with
   * @param insertionPoint  The linearlayout at which to insert the rows of packs
   */
  private void populatePackLayout(List<String> packlist, LinearLayout insertionPoint) {
    String packname;
    int count = 0;
    
    // Instantiate all our views for programmatic layout creation    
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);
    
    for (Iterator<String> it = packlist.iterator(); it.hasNext();) {
      packname = it.next();
      Log.d(TAG, "Count: " + count + "\nPackname: " + packname);
      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.packpurchaserow, layout);
      RelativeLayout packRow = (RelativeLayout) line.getChildAt(count);

      // Make every line alternating color
      if (count % 2 == 0) {
        View background = (View) packRow.getChildAt(0);
        background.setBackgroundResource(R.color.genericBG_trim);
      }
      
      // Set Pack Title
      TextView packTitle = (TextView) packRow.getChildAt(1);
      //TODO this will need to pull the string from our pack list
      packTitle.setText(packname);

      // Set Row end icon
      ImageView packIcon = (ImageView) packRow.getChildAt(3);
      packIcon.setImageResource(R.drawable.gameend_row_end_green);
      mPackViewList.add(packIcon);
      mPackLineList.add(packRow);
      
      // Bind Listener
      packRow.setOnClickListener(mPhrasePackListener);
      count++;
    }
    insertionPoint.addView(layout);   
    
  }
  
  /**
   * This listener will set each row to its appropriate behavior, with freemium
   * apps needing share intents bound and paid apps needing purchase intents bound.
   */
  private final OnClickListener mPhrasePackListener = new OnClickListener() {
    public void onClick(View v) {
      int packIndex = mPackLineList.indexOf(v);
      if (PhraseCrazeApplication.DEBUG) {
        Log.d(TAG, "FreePackLineIndex: " + Integer.toString(packIndex));
      }
      
      //TODO This is where we would need to retrieve the pack index
      //Card curCard = mCardList.get(cardIndex);
      
      Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SUBJECT TEXT HERE");
      shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "MESSAGE TEXT HERE");

      startActivity(Intent.createChooser(shareIntent, "Choose Sharing Platform"));
    }
  };
}
