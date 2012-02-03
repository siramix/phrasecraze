package com.siramix.phrasecraze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.siramix.phrasecraze.Consts;
import com.siramix.phrasecraze.PurchaseObserver;
import com.siramix.phrasecraze.BillingService.RequestPurchase;
import com.siramix.phrasecraze.BillingService.RestoreTransactions;
import com.siramix.phrasecraze.Consts.PurchaseState;
import com.siramix.phrasecraze.Consts.ResponseCode;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhrasePackPurchase extends Activity {

  private static final String TAG = "CardPackPurchase";

public static final String DB_INITIALIZED = "com.siramix.phrasecraze.DB_INITIALIZED";
  
  // To be used for tooltips to help guide users
  private Toast mHelpToast = null;
  
  private List<String> mPackList;  // This must stay in sync with mPackLineList
  List<ImageView> mPackViewList;
  List<View> mPackLineList;
  
  /**
   * This block of maps stores our lists of clients
   */
  HashMap<String, String> mKnownTwitterClients;
  HashMap<String, String> mKnownFacebookClients;
  HashMap<String, String> mKnownGoogleClients;
  HashMap<String, ActivityInfo> mFoundTwitterClients; 
  HashMap<String, ActivityInfo> mFoundFacebookClients;
  HashMap<String, ActivityInfo> mFoundGoogleClients; 

  /**
   * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
   * messages to this application so that we can update the UI.
   */
  private class PhrasePackPurchaseObserver extends PurchaseObserver {
      public PhrasePackPurchaseObserver(Activity activity, Handler handler) {
        super(activity, handler);
      }

      @Override
      public void onBillingSupported(boolean supported) {
          if (Consts.DEBUG) {
              Log.i(TAG, "supported: " + supported);
          }
          if (supported) {
              //restoreDatabase();
          } else {
              showToast("Billing not Supported");
          }
      }

      @Override
      public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
              int quantity, long purchaseTime, String developerPayload) {
          if (Consts.DEBUG) {
              Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
          }

          if (developerPayload == null) {
              //logProductActivity(itemId, purchaseState.toString());
          } else {
              //logProductActivity(itemId, purchaseState + "\n\t" + developerPayload);
          }

          if (purchaseState == PurchaseState.PURCHASED) {
              //mOwnedItems.add(itemId);
          }
          //mCatalogAdapter.setOwnedItems(mOwnedItems);
          //mOwnedItemsCursor.requery();
      }

      @Override
      public void onRequestPurchaseResponse(RequestPurchase request,
              ResponseCode responseCode) {
          if (Consts.DEBUG) {
              Log.d(TAG, request.mProductId + ": " + responseCode);
          }
          if (responseCode == ResponseCode.RESULT_OK) {
              if (Consts.DEBUG) {
                  Log.i(TAG, "purchase was successfully sent to server");
              }
              //logProductActivity(request.mProductId, "sending purchase request");
          } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
              if (Consts.DEBUG) {
                  Log.i(TAG, "user canceled purchase");
              }
              //logProductActivity(request.mProductId, "dismissed purchase dialog");
          } else {
              if (Consts.DEBUG) {
                  Log.i(TAG, "purchase failed");
              }
              //logProductActivity(request.mProductId, "request purchase returned " + responseCode);
          }
      }

      @Override
      public void onRestoreTransactionsResponse(RestoreTransactions request,
              ResponseCode responseCode) {
          if (responseCode == ResponseCode.RESULT_OK) {
              if (Consts.DEBUG) {
                  Log.d(TAG, "completed RestoreTransactions request");
              }
              // Update the shared preferences so that we don't perform
              // a RestoreTransactions again.
              SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
              SharedPreferences.Editor edit = prefs.edit();
              edit.putBoolean(DB_INITIALIZED, true);
              edit.commit();
          } else {
              if (Consts.DEBUG) {
                  Log.d(TAG, "RestoreTransactions error: " + responseCode);
              }
          }
      }
  }

  private PhrasePackPurchaseObserver mPurchaseObserver;
  private Handler mHandler;
  private BillingService mBillingService;

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
    
    // Detect Social Clients
    detectClients();
    
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
    
    mHandler = new Handler();
    mPurchaseObserver = new PhrasePackPurchaseObserver(this,mHandler);
    mBillingService = new BillingService();
    mBillingService.setContext(this);

    // Check if billing is supported.
    ResponseHandler.register(mPurchaseObserver);
    if (!mBillingService.checkBillingSupported()) {
        showToast("NO BILLING!");
    } else {
        showToast("HURRAY BILLING!");
    }
    mBillingService.requestPurchase("foo", "bar");
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
      //TODO this will need to be more specific later (to just free social apps)
      if (count == 0) {
        packRow.setOnClickListener(mTweetListener);
      }
      else if (count == 1) {
        packRow.setOnClickListener(mFacebookListener);
      }
      else if (count == 2) {
        packRow.setOnClickListener(mGoogleListener);
      }
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
      ComponentName targetComponent = getClientComponentName(mFoundTwitterClients);

      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = (targetComponent.getClassName().contains("com.twidroid")) ?
            "application/twitter" : "text/plain";
        intent.setType(intentType);        
        intent.putExtra(Intent.EXTRA_TEXT, "TESTING TESTING \n https://market.android.com/details?id=com.buzzwords");
        startActivityForResult(intent, 0);
      } else {
        showToast(getString(R.string.toast_packpurchase_notwitter));
      }
    }
  };
  
  /**
   * This listener is specifically for packs that require posting on facebook to get.
   */
  private final OnClickListener mFacebookListener = new OnClickListener() {    
    //Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getClientComponentName(mFoundFacebookClients);

      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = ("text/plain");
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT SUBJECT" + "\n" + "TESTING");
        intent.putExtra(Intent.EXTRA_TEXT, "TESTING TESTING" + "\n" + "TESTING");
        intent.putExtra(Intent.EXTRA_TEXT, "https://market.android.com/details?id=com.buzzwords");
        startActivity(intent);
      } else {
        showToast(getString(R.string.toast_packpurchase_nofacebook));
      }
    }
  };

  /**
   * This listener is specifically for packs that require google+ posting to get.
   */
  private final OnClickListener mGoogleListener = new OnClickListener() {    
    //Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getClientComponentName(mFoundGoogleClients);
 
      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = ("text/plain");
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT SUBJECT" + "\n" + "TESTING");
        intent.putExtra(Intent.EXTRA_TEXT, "TESTING TESTING \n https://market.android.com/details?id=com.buzzwords");
        startActivityForResult(intent, 0);
      } else {
        showToast(getString(R.string.toast_packpurchase_nogoogleplus));
      }
    }  
  };
  
  /**
   * Listen for the result of social activities like twitter, facebook, and google+
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    Log.d(TAG, "****** ACTIVITY RESULT RESULTCODE = " + resultCode);    

    //TODO obviously handle this better
    if (resultCode == 0) {
      showToast("WE'RE GIVING YOU THIS PACK NOW!!!");
    }
    
    if (data != null) {
      // launch the application that we just picked
      startActivity(data);
   }    
  }
  
  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/ 
   */
  private void buildKnownClientsList() {    
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "buildKnownClientsList()");
    }
    
    mKnownTwitterClients = new HashMap<String, String>();
    mKnownTwitterClients.put("Twitter", "com.twitter.android.PostActivity");
    mKnownTwitterClients.put("UberSocial", "com.twidroid.activity.SendTweet");
    mKnownTwitterClients.put("TweetDeck", "com.tweetdeck.compose.ComposeActivity");
    mKnownTwitterClients.put("Seesmic", "com.seesmic.ui.Composer");
    mKnownTwitterClients.put("TweetCaster", "com.handmark.tweetcaster.ShareSelectorActivity");
    mKnownTwitterClients.put("Plume", "com.levelup.touiteur.appwidgets.TouiteurWidgetNewTweet");
    mKnownTwitterClients.put("Twicca", "jp.r246.twicca.statuses.Send");
    mKnownFacebookClients = new HashMap<String, String>();
    mKnownFacebookClients.put("Facebook", "com.facebook.katana.ShareLinkActivity");       
    mKnownFacebookClients.put("FriendCaster", "uk.co.senab.blueNotifyFree.activity.PostToFeedActivity");
    mKnownGoogleClients = new HashMap<String, String>();  
    mKnownGoogleClients.put("Google+", "com.google.android.apps.plus.phone.PostActivity");    
  }
  
  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/ 
   * @return
   */
  public void detectClients() {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "detectClients()");
    }
    
    buildKnownClientsList();
    mFoundTwitterClients = new HashMap<String, ActivityInfo>();
    mFoundFacebookClients = new HashMap<String, ActivityInfo>();
    mFoundGoogleClients = new HashMap<String, ActivityInfo>();
   
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);
    
    for (int i = 0; i < activityList.size(); i++) {
      ResolveInfo app = (ResolveInfo) activityList.get(i);
      ActivityInfo activity = app.activityInfo;
      Log.d(TAG, "******* --> " + activity.name );
      if (mKnownTwitterClients.containsValue(activity.name)) {
        mFoundTwitterClients.put(activity.name, activity);
      }
      else if (mKnownFacebookClients.containsValue(activity.name)) {
        mFoundFacebookClients.put(activity.name, activity);
      }
      else if (mKnownGoogleClients.containsValue(activity.name)) {
        mFoundGoogleClients.put(activity.name, activity);
      }
    }    
  }
 
  /**
   * Returns the Component name of either Twitter, Google, or Facebook
   * @param foundClients A hashmap of clients that have been identified by 
   *                     Detect Clients as being on the users phone
   * @return
   */
  public ComponentName getClientComponentName(HashMap<String, ActivityInfo> foundClients) {    
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getClientComponentName()");
    }
    
    ComponentName result = null;

    if (foundClients.size() > 0) {
      ActivityInfo socialActivity = null;
      for(Map.Entry<String, ActivityInfo> entry : foundClients.entrySet()) {
        socialActivity = entry.getValue();
        break;
      }
   
      result = new ComponentName(socialActivity.applicationInfo.packageName, socialActivity.name);
    }

    return result;
  }

  
  /**
   * Handle showing a toast or refreshing an existing toast
   */
  private void showToast(String text) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "showToast(" + text + ")");
    }
    
    if(mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }
}


