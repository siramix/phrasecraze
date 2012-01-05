package com.siramix.phrasecraze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
  
  HashMap<String, String> knownTwitterClients; // For sharing packs on Twitter
  HashMap<String, ActivityInfo> foundTwitterClients; // To compare against known clients
  
  /**
   * Create the packages screen from an XML layout and
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
    
    // Detect Twitter Clients
    detectTwitterClients();
    
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
      packRow.setOnClickListener(mTweetListener);
      count++;
    }
    insertionPoint.addView(layout);   
    
  }
  
  /**
   * This listener is specifically for packs that require tweeting to get.
   */
  private final OnClickListener mTweetListener = new OnClickListener() {    
    //Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getTwitterClientComponentName();

      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = (targetComponent.getClassName().contains("com.twidroid")) ?
            "application/twitter" : "text/plain";
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_TEXT, "TESTING TESTING" + "\n" + "TESTING");
        startActivityForResult(intent, 0);
      } else {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "TESTING TESTING" + "\n" + "TESTING");
        startActivityForResult(Intent.createChooser(intent, "Share..."), 0);
      }
    }
  };

  /**
   * Listen for the result of social activities like twitter, facebook, and google+
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    Log.d(TAG, "****** ACTIVITY RESULT RESULTCODE = " + resultCode);
    Log.d(TAG, "****** ACTIVITY RESULT RESULTCODE = " + resultCode);
 /* if (requestCode == PICK_CONTACT_REQUEST) {
      if (resultCode == RESULT_OK) {
          // A contact was picked.  Here we will just display it
          // to the user.
          startActivity(new Intent(Intent.ACTION_VIEW, data));
      }
  }*/
  }
  
  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/ 
   */
  private void buildKnownTwitterClientsList() {
    knownTwitterClients = new HashMap<String, String>();
    knownTwitterClients.put("Twitter", "com.twitter.android.PostActivity");
    knownTwitterClients.put("UberSocial", "com.twidroid.activity.SendTweet");
    knownTwitterClients.put("TweetDeck", "com.tweetdeck.compose.ComposeActivity");
    knownTwitterClients.put("Seesmic", "com.seesmic.ui.Composer");
    knownTwitterClients.put("TweetCaster", "com.handmark.tweetcaster.ShareSelectorActivity");
    knownTwitterClients.put("Plume", "com.levelup.touiteur.appwidgets.TouiteurWidgetNewTweet");
    knownTwitterClients.put("Twicca", "jp.r246.twicca.statuses.Send");
  }
  
  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/ 
   * @return
   */
  public boolean detectTwitterClients() {
    buildKnownTwitterClientsList();
    foundTwitterClients = new HashMap<String, ActivityInfo>();
   
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);
    
    for (int i = 0; i < activityList.size(); i++) {
      ResolveInfo app = (ResolveInfo) activityList.get(i);
      ActivityInfo activity = app.activityInfo;
      if (knownTwitterClients.containsValue(activity.name)) {
        foundTwitterClients.put(activity.name, activity);
      }
    }
    
    return false;
  }
 
  //Resolve the twitter client component name
  public ComponentName getTwitterClientComponentName() {
    ComponentName result = null;

    if (foundTwitterClients.size() > 0) {
      ActivityInfo tweetActivity = null;
      for(Map.Entry<String, ActivityInfo> entry : foundTwitterClients.entrySet()) {
        tweetActivity = entry.getValue();
        break;
      }
   
      result = new ComponentName(tweetActivity.applicationInfo.packageName, tweetActivity.name);
    }

    return result;
  }

}


