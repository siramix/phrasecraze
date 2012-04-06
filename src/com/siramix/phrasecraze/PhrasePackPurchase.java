package com.siramix.phrasecraze;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhrasePackPurchase extends Activity {

  private static final String TAG = "CardPackPurchase";
  public static final String DB_INITIALIZED = "com.siramix.phrasecraze.DB_INITIALIZED";

  // To be used for tooltips to help guide users
  private Toast mHelpToast = null;

  List<View> mPackLineList;

  private SharedPreferences mPackPrefs;

  // A map of request codes and their corresponding packs
  private HashMap<Integer, Pack> mSocialPacks;

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
   * Request Code constants for social media sharing
   */
  private static final int TWITTER_REQUEST_CODE = 11;
  private static final int FACEBOOK_REQUEST_CODE = 12;
  private static final int GOOGLEPLUS_REQUEST_CODE = 13;

  /**
   * PlayGameListener plays an animation on the view that will result in
   * launching GameSetup
   */
  private OnClickListener mGameSetupListener = new OnClickListener() {
    public void onClick(View v) {
      Log.d(TAG, "PlayGameListener OnClick()");

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PhrasePackPurchase.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      Map<String, ?> packSelections = new HashMap<String, Boolean>();
      packSelections = mPackPrefs.getAll();

      boolean anyPackSelected = false;
      for (String packId : packSelections.keySet()) {
        if (mPackPrefs.getBoolean(packId, false) == true) {
          anyPackSelected = true;
        }
      }

      // Only advance to next screen if a pack is selected
      if (anyPackSelected == true) {
        startActivity(new Intent(PhrasePackPurchase.this.getApplication()
            .getString(R.string.IntentGameSetup), getIntent().getData()));
      } else {
        showToast(getString(R.string.toast_packpurchase_nopackselected));
      }
    }
  };

  /**
   * A {@link PurchaseObserver} is used to get callbacks when Android Market
   * sends messages to this application so that we can update the UI.
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
        // restoreDatabase();
      } else {
        showToast("Billing not Supported");
      }
    }

    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,
        String itemId, int quantity, long purchaseTime, String developerPayload) {
      if (Consts.DEBUG) {
        Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " "
            + purchaseState);
      }

      if (developerPayload == null) {
        // logProductActivity(itemId, purchaseState.toString());
      } else {
        // logProductActivity(itemId, purchaseState + "\n\t" +
        // developerPayload);
      }

      if (purchaseState == PurchaseState.PURCHASED) {
        // mOwnedItems.add(itemId);
      }
      // mCatalogAdapter.setOwnedItems(mOwnedItems);
      // mOwnedItemsCursor.requery();
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
        // logProductActivity(request.mProductId, "sending purchase request");
      } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
        if (Consts.DEBUG) {
          Log.i(TAG, "user canceled purchase");
        }
        // logProductActivity(request.mProductId, "dismissed purchase dialog");
      } else {
        if (Consts.DEBUG) {
          Log.i(TAG, "purchase failed");
        }
        // logProductActivity(request.mProductId, "request purchase returned " +
        // responseCode);
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
    Log.d(TAG, "onCreate()");

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Get our pack preferences
    mPackPrefs = getSharedPreferences(Consts.PREF_PACK_SELECTIONS,
        Context.MODE_PRIVATE);

    // Setup the view
    this.setContentView(R.layout.packpurchase);

    // Detect Social Clients
    detectClients();

    // set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");
    TextView header = (TextView) this.findViewById(R.id.PackPurchase_Title);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_UnlockedPackTitle);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_PaidPackTitle);
    header.setTypeface(antonFont);

    // Instantiate all of our lists for programmatic adding of packs to view
    mPackLineList = new LinkedList<View>();

    refreshAllPackLayouts();
    // mBillingService.requestPurchase("test_pack", "payload_test");
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshAllPackLayouts();
  }

  private void refreshAllPackLayouts() {
    // Get our current context
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();

    GameManager game = application.getGameManager();

    // Populate and display list of cards
    LinearLayout unlockedPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_UnlockedPackSets);
    LinearLayout paidPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_PaidPackSets);

    unlockedPackLayout.removeAllViewsInLayout();
    paidPackLayout.removeAllViewsInLayout();

    PackClient client = PackClient.getInstance();
    LinkedList<Pack> serverPacks = new LinkedList<Pack>();
    LinkedList<Pack> localPacks = new LinkedList<Pack>();
    localPacks = game.getInstalledPacks();
    mSocialPacks = new HashMap<Integer, Pack>();

    // First try to get the online packs, if no internet, just use local packs
    try {
      serverPacks.addAll(client.getPayPacks());
      serverPacks.addAll(client.getFreePacks());
      populatePackLayout(localPacks, unlockedPackLayout);
      populatePackLayout(serverPacks, paidPackLayout);
    } catch (IOException e1) {
      populatePackLayout(localPacks, unlockedPackLayout);
      showToast(getString(R.string.toast_packpurchase_nointerneterror));
      e1.printStackTrace();
    } catch (URISyntaxException e1) {
      populatePackLayout(localPacks, unlockedPackLayout);
      showToast(getString(R.string.toast_packpurchase_siramixdownerror));
      e1.printStackTrace();
    } catch (JSONException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    mHandler = new Handler();
    mPurchaseObserver = new PhrasePackPurchaseObserver(this, mHandler);
    mBillingService = new BillingService();
    mBillingService.setContext(this);

    // Check if billing is supported.
    ResponseHandler.register(mPurchaseObserver);

    Button btn = (Button) this.findViewById(R.id.PackPurchase_Button_Next);
    btn.setOnClickListener(mGameSetupListener);
  }

  /**
   * Create dynamic rows of packs at runtime for pack purchase view. This will
   * set up the XML, bind listeners, and update pack titles, price, and images
   * according to the information passed in. Retrieving the info to populate
   * should be the responsibility of a different method.
   * 
   * @param packlist
   *          A list of packs to iterate through and populate the purchase rows
   *          with
   * @param insertionPoint
   *          The linearlayout at which to insert the rows of packs
   */
  private void populatePackLayout(List<Pack> packlist,
      LinearLayout insertionPoint) {
    int count = 0;

    // Instantiate all our views for programmatic layout creation
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    for (Iterator<Pack> it = packlist.iterator(); it.hasNext();) {
      Pack curPack = it.next();

      Log.d(TAG, "Count: " + count + "\nPackname: " + curPack.getName());

      // Create a new row for this pack
      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.packpurchaserow, layout);
      PackPurchaseRowLayout row = (PackPurchaseRowLayout) line
          .getChildAt(count);
      row.setPack(curPack, getPackPref(curPack), count % 2 == 0);

      // Add pack rows to the list. Give margin so borders don't double up
      LinearLayout.LayoutParams margin = (LinearLayout.LayoutParams) row
          .getLayoutParams();
      final float DENSITY = this.getResources().getDisplayMetrics().density;
      margin.setMargins(0, (int) (-2 * DENSITY), 0, 0);
      row.setLayoutParams(margin);
      mPackLineList.add(row);

      // Bind Listener
      // TODO this will need to be more specific later (to just free social
      // apps)
      if (curPack.getPath().equals("freepacks/twitter.json")) {
        row.setOnClickListener(mTweetListener);
        mSocialPacks.put(TWITTER_REQUEST_CODE, curPack);
      } else if (curPack.getPath().equals("freepacks/facebook.json")) {
        row.setOnClickListener(mFacebookListener);
        mSocialPacks.put(FACEBOOK_REQUEST_CODE, curPack);
      } else if (curPack.getPath().equals("freepacks/googleplus.json")) {
        row.setOnClickListener(mGoogleListener);
        mSocialPacks.put(GOOGLEPLUS_REQUEST_CODE, curPack);
      } else if (curPack.isInstalled()) {
        row.setOnPackSelectedListener(mSelectPackListener);
        row.setOnPackInfoRequestedListener(mPackInfoListener);
      } else {
        row.setOnClickListener(mPremiumPackListener);
      }

      count++;
    }
    insertionPoint.addView(layout);
    
    // Initially update the bar percentages to match the preferences
    updateComboBarPercentages();
  }
  
  /*
   * Update the views that display the percentages for Selected, Unselected,
   * and Locked phrases
   */
  private void updateComboBarPercentages() {
    PhraseCrazeApplication application = (PhraseCrazeApplication) this
        .getApplication();
    GameManager game = application.getGameManager();
    ComboPercentageBar bar = (ComboPercentageBar) this
        .findViewById(R.id.PackPurchase_PhraseBars);

    // Set bar title
    bar.setTitle("Selected Phrases");

    // ToDo: Get these getting the correct packs.
    // Is this the best way to get installed packs?
    LinkedList<Pack> localPacks = new LinkedList<Pack>();
    localPacks = game.getInstalledPacks();

    int numSelected = 0;
    int numUnSelected = 0;
    int numLocked = 3000;
    for (int i = 0; i < localPacks.size(); i++) {
      if (getPackPref(localPacks.get(i))) {
        // numSelected += localPacks.get(i).getNumPlayablePhrases();
        numSelected += 1000;
      } else {
        // numUnSelected += localPacks.get(i).getNumPlayablePhrases();
        numUnSelected += 1000;
      }
    }

    // Get color for the Unselected bar. This changes to red
    // when no phrases are selected
    int colorUnselected = this.getResources().getColor(
        R.color.packPurchaseUnSelected);
    if (numSelected == 0) {
      colorUnselected = this.getResources().getColor(R.color.teamC_primary);
    }

    // Set bar elements
    bar.setSegmentComponents(0, numSelected, "Selected", this.getResources()
        .getColor(R.color.packPurchaseSelected),
        this.getResources().getColor(R.color.text_subtext));
    bar.setSegmentComponents(1, numUnSelected, "Unselected", colorUnselected,
        this.getResources().getColor(R.color.text_subtext));
    bar.setSegmentComponents(2, numLocked, "Locked", this.getResources()
        .getColor(R.color.genericBG_trimDark),
        this.getResources().getColor(R.color.text_subtext));

    // ReRender bar
    bar.updateSegmentWeights();
  }

  /**
   * This listener is specifically for packs that require tweeting to get.
   */
  private final OnClickListener mTweetListener = new OnClickListener() {
    // Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getClientComponentName(mFoundTwitterClients);

      if (targetComponent != null) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setComponent(targetComponent);
        String intentType = (targetComponent.getClassName()
            .contains("com.twidroid")) ? "application/twitter" : "text/plain";
        shareIntent.setType(intentType);
        shareIntent
            .putExtra(Intent.EXTRA_TEXT,
                "TESTING TESTING \n https://market.android.com/details?id=com.buzzwords");
        Pack curPack = (Pack) v.getTag();
        shareIntent.putExtra(getString(R.string.packBundleKey), curPack);
        startActivityForResult(shareIntent, TWITTER_REQUEST_CODE);
      } else {
        showToast(getString(R.string.toast_packpurchase_notwitter));
      }
    }
  };

  /**
   * This listener is specifically for packs that require posting on facebook to
   * get.
   */
  private final OnClickListener mFacebookListener = new OnClickListener() {
    // Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getClientComponentName(mFoundFacebookClients);

      // TODO intent is a stupid name
      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = ("text/plain");
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT SUBJECT" + "\n"
            + "TESTING");
        intent
            .putExtra(Intent.EXTRA_TEXT, "TESTING TESTING" + "\n" + "TESTING");
        intent.putExtra(Intent.EXTRA_TEXT,
            "https://market.android.com/details?id=com.buzzwords");
        startActivityForResult(intent, FACEBOOK_REQUEST_CODE);
      } else {
        showToast(getString(R.string.toast_packpurchase_nofacebook));
      }
    }
  };

  /**
   * This listener is specifically for packs that require google+ posting to
   * get.
   */
  private final OnClickListener mGoogleListener = new OnClickListener() {
    // Tweet button handler
    public void onClick(View v) {
      ComponentName targetComponent = getClientComponentName(mFoundGoogleClients);

      // TODO intent is a stupid name
      if (targetComponent != null) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(targetComponent);
        String intentType = ("text/plain");
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT SUBJECT" + "\n"
            + "TESTING");
        intent
            .putExtra(Intent.EXTRA_TEXT,
                "TESTING TESTING \n https://market.android.com/details?id=com.buzzwords");
        startActivityForResult(intent, GOOGLEPLUS_REQUEST_CODE);
      } else {
        showToast(getString(R.string.toast_packpurchase_nogoogleplus));
      }
    }
  };

  /**
   * Listen for the result of social activities like twitter, facebook, and
   * google+
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "****** ACTIVITY RESULT RESULTCODE = " + resultCode);

    // TODO obviously handle this better
    if (resultCode == 0) {
      PhraseCrazeApplication application = (PhraseCrazeApplication) this
          .getApplication();
      GameManager game = application.getGameManager();
      Pack curPack = mSocialPacks.get(requestCode);
      // TODO: Catch the runtime exception
      try {
        game.getDeck().digestPack(curPack);
      } catch (RuntimeException e) {
        e.printStackTrace();
      }

      showToast(mSocialPacks.get(requestCode).getName());
      if (getPackPref(curPack)) {
        setPackPref(curPack, false);
      } else {
        setPackPref(curPack, true);
      }
    }

    /*
     * if (data != null) { // launch the application that we just picked
     * startActivity(data); }
     */
  }

  /**
   * This listener is specifically for packs that require purchasing to get.
   */
  private final OnClickListener mPremiumPackListener = new OnClickListener() {
    // Tweet button handler
    public void onClick(View v) {
      Pack curPack = ((PackPurchaseRowLayout) v).getPack();
      mBillingService.requestPurchase(curPack.getPath(), "payload_test");
    }
  };

  /*
   * Listener for the pack selection, which includes or excludes the pack
   * from the deck.
   */
  private final OnPackSelectedListener mSelectPackListener = new OnPackSelectedListener() {

    @Override
    public void onPackSelected(Pack pack, boolean selectionStatus) {
      setPackPref(pack, selectionStatus);
      
      // Refresh weightings on the bars
      updateComboBarPercentages();

      // play confirm sound when points are added
      SoundManager sm = SoundManager.getInstance(PhrasePackPurchase.this
          .getBaseContext());
      if (selectionStatus) {
        sm.playSound(SoundManager.Sound.CONFIRM);
      } else {
        sm.playSound(SoundManager.Sound.BACK);
      }
    }
  };

  /*
   * Listener that brings up pack info
   */
  private final OnPackInfoRequestedListener mPackInfoListener = new OnPackInfoRequestedListener() {

    @Override
    public void onPackInfoRequested(Pack pack) {
      // play confirm sound when points are added
      SoundManager sm = SoundManager.getInstance(PhrasePackPurchase.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
     
      // Show pack info activity
      showPackInfo(pack); 
    }
  };

  /*
   * Shows the packInfo activity. It takes a row that is to be displayed as
   * a parameter.
   */
  private void showPackInfo(Pack pack)
  {
    boolean selectionStatus = getPackPref(pack);
    // For now, we don't care if the row background matches
    boolean isPackRowOdd = true; 

    // Show Set Buzzed Team Dialog
    Intent intent = new Intent(getApplication().getString(
        R.string.IntentPackInfo), getIntent().getData());
    // Pass in that the choice is not required
    intent.putExtra(getApplication().getString(R.string.packBundleKey),
        pack);
    intent.putExtra(
        getApplication().getString(R.string.packInfoIsPackSelectedBundleKey),
        selectionStatus);
    intent.putExtra(
        getApplication().getString(R.string.packInfoIsPackRowOddBundleKey),
        isPackRowOdd);
    startActivity(intent);
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
    mKnownTwitterClients.put("TweetDeck",
        "com.tweetdeck.compose.ComposeActivity");
    mKnownTwitterClients.put("Seesmic", "com.seesmic.ui.Composer");
    mKnownTwitterClients.put("TweetCaster",
        "com.handmark.tweetcaster.ShareSelectorActivity");
    mKnownTwitterClients.put("Plume",
        "com.levelup.touiteur.appwidgets.TouiteurWidgetNewTweet");
    mKnownTwitterClients.put("Twicca", "jp.r246.twicca.statuses.Send");
    mKnownFacebookClients = new HashMap<String, String>();
    mKnownFacebookClients.put("Facebook",
        "com.facebook.katana.ShareLinkActivity");
    mKnownFacebookClients.put("FriendCaster",
        "uk.co.senab.blueNotifyFree.activity.PostToFeedActivity");
    mKnownGoogleClients = new HashMap<String, String>();
    mKnownGoogleClients.put("Google+",
        "com.google.android.apps.plus.phone.PostActivity");
  }

  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/
   * 
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
      Log.d(TAG, "******* --> " + activity.name);
      if (mKnownTwitterClients.containsValue(activity.name)) {
        mFoundTwitterClients.put(activity.name, activity);
      } else if (mKnownFacebookClients.containsValue(activity.name)) {
        mFoundFacebookClients.put(activity.name, activity);
      } else if (mKnownGoogleClients.containsValue(activity.name)) {
        mFoundGoogleClients.put(activity.name, activity);
      }
    }
  }

  /**
   * Get the current value of the pack preferences for a given pack name
   * 
   * @param packName
   * @return
   */
  public boolean getPackPref(Pack pack) {
    Log.d(TAG, "getPackPref(" + pack.getName() + ")");
    return mPackPrefs.getBoolean(String.valueOf(pack.getId()), false);
  }

  /**
   * Change the pack preference for the passed in pack to either on or off.
   * 
   * @param curPack
   *          the pack whose preference will be changed
   */
  public void setPackPref(Pack pack, boolean onoff) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "setPackPref(" + pack.getName() + "," + onoff + ")");
    }
    // Store the pack's boolean in the preferences file for pack preferences
    SharedPreferences.Editor packPrefsEdit = mPackPrefs.edit();
    packPrefsEdit.putBoolean(String.valueOf(pack.getId()), onoff);
    packPrefsEdit.commit();
  }

  /**
   * Returns the Component name of either Twitter, Google, or Facebook
   * 
   * @param foundClients
   *          A hashmap of clients that have been identified by Detect Clients
   *          as being on the users phone
   * @return
   */
  public ComponentName getClientComponentName(
      HashMap<String, ActivityInfo> foundClients) {
    if (PhraseCrazeApplication.DEBUG) {
      Log.d(TAG, "getClientComponentName()");
    }

    ComponentName result = null;

    if (foundClients.size() > 0) {
      ActivityInfo socialActivity = null;
      for (Map.Entry<String, ActivityInfo> entry : foundClients.entrySet()) {
        socialActivity = entry.getValue();
        break;
      }

      result = new ComponentName(socialActivity.applicationInfo.packageName,
          socialActivity.name);
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

    if (mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text,
          Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      // TODO Can we get these toasts to display in a different spot?
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }

}
