package com.siramix.phrasecraze;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.siramix.phrasecraze.BillingService.RequestPurchase;
import com.siramix.phrasecraze.BillingService.RestoreTransactions;
import com.siramix.phrasecraze.Consts.PurchaseState;
import com.siramix.phrasecraze.Consts.ResponseCode;

public class PhrasePackPurchase extends Activity {

  private static final String TAG = "PhrasePackPurchase";
  
  private ProgressDialog mInstallDialog;
  
  // To be used for tooltips to help guide users
  private Toast mHelpToast = null;

  List<View> mPackLineList;

  private SharedPreferences mPackPrefs;
  
  // Our pack lists as retrieved from the server
  private LinkedList<Pack> mFreePacks;
  private LinkedList<Pack> mPayPacks;

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
  private static final int PACKINFO_REQUEST_CODE = 14;

  /**
   * Supports in-app billing
   */
  private PhrasePackPurchaseObserver mPurchaseObserver;
  private Handler mHandler;
  private BillingService mBillingService;
  boolean mBillingSupported;
  

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
      Log.d(TAG, "supported: " + supported);

      if (supported) {
        restorePacks();
        mBillingSupported = true;
      } else {
        showToast("Billing not Supported");
        mBillingSupported = false;
      }
    }

    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
            int quantity, long purchaseTime, String developerPayload) {
        Log.d(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
        
        if (developerPayload == null) {
            //logProductActivity(itemId, purchaseState.toString());
        } else {
            //logProductActivity(itemId, purchaseState + "\n\t" + developerPayload);
        }
        // TODO DEBUG CODE REMOVE BEFORE PRODUCTION
        if (itemId.contentEquals("hott.json")) {
          itemId = "1001";
        }
        
        if (purchaseState == PurchaseState.PURCHASED) {
          // TODO DEBUG CODE REMOVE BEFORE PRODUCTION
          if (itemId.contains("android")) {
            installPack(itemId);
          } else{
            installPack(Integer.parseInt(itemId));
            //mOwnedItems.add(itemId);
            
          }
        }
        else if (purchaseState == PurchaseState.CANCELED) {
          // TODO remove pack if purchase is cancelled
          if (itemId.contains("android")) {
            uninstallPack(itemId);
          } else{
            uninstallPack(Integer.parseInt(itemId));
            //mOwnedItems.add(itemId);
          }
        }
        else if (purchaseState == PurchaseState.REFUNDED) {
          // TODO remove pack if purchase is refunded
          if (itemId.contains("android")) {
            uninstallPack(itemId);
          } else{
            uninstallPack(Integer.parseInt(itemId));
            //mOwnedItems.add(itemId);
          }
        }
        
        //mCatalogAdapter.setOwnedItems(mOwnedItems);
        //mOwnedItemsCursor.requery();
    }

    @Override
    public void onRequestPurchaseResponse(RequestPurchase request,
          ResponseCode responseCode) {
      Log.d(TAG, request.mProductId + ": " + responseCode);

      if (responseCode == ResponseCode.RESULT_OK) {
          if (Consts.DEBUG) {
            Log.i(TAG, "Purchase of " + request.mProductId + " was successfully sent to server");
          }
          //logProductActivity(request.mProductId, "sending purchase request");
      } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
          if (Consts.DEBUG) {
            Log.i(TAG, "User canceled purchase of " + request.mProductId);
          }
          //logProductActivity(request.mProductId, "dismissed purchase dialog");
      } else {
          if (Consts.DEBUG) {
            Log.i(TAG, "purchase of " + request.mProductId + " failed");
          }
          //logProductActivity(request.mProductId, "request purchase returned " + responseCode);
      }
    }

    @Override
    public void onRestoreTransactionsResponse(RestoreTransactions request,
          ResponseCode responseCode) {
      if (responseCode == ResponseCode.RESULT_OK) {
          Log.d(TAG, "completed RestoreTransactions request");

          // Update the shared preferences so that we don't perform
          // a RestoreTransactions again.
          SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
          SharedPreferences.Editor edit = prefs.edit();
          edit.putBoolean(Consts.PREFKEY_PACKS_INITIALIZED, true);
          edit.commit();
      } else {
          if (Consts.DEBUG) {
              Log.d(TAG, "RestoreTransactions error: " + responseCode);
          }
        }
      }
    }

  /**
   * Create the packages screen from an XML layout and
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate()");

    // Initialize our packs
    mFreePacks = new LinkedList<Pack>();
    mPayPacks = new LinkedList<Pack>();
    
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Get our pack preferences
    mPackPrefs = getSharedPreferences(Consts.PREFKEY_PACK_SELECTIONS,
        Context.MODE_PRIVATE);

    // Setup the view
    this.setContentView(R.layout.packpurchase);

    // Detect Social Clients
    detectClients();
    
    // Initialize billing service
    mHandler = new Handler();
    mPurchaseObserver = new PhrasePackPurchaseObserver(this,mHandler);
    mBillingService = new BillingService();
    mBillingService.setContext(this);
    
    // Check if billing is supported.
    ResponseHandler.register(mPurchaseObserver);
    if (!mBillingService.checkBillingSupported()) {
      showToast(getString(R.string.toast_packpurchase_nointerneterror));
    }
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
    ResponseHandler.register(mPurchaseObserver);
  }

  protected void refreshAllPackLayouts() {
    Log.d(TAG, "refreshAllPackLayouts");
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
    LinkedList<Pack> lockedPacks = new LinkedList<Pack>();
    LinkedList<Pack> localPacks = new LinkedList<Pack>();
    localPacks = game.getInstalledPacks();

    // First try to get the online packs, if no internet, just use local packs
    try {
      mFreePacks = client.getFreePacks();
      mPayPacks = client.getPayPacks();
      lockedPacks.addAll(mPayPacks);
      lockedPacks.addAll(mFreePacks);
      lockedPacks = removeLocalPacks(lockedPacks, localPacks);
      populatePackLayout(localPacks, unlockedPackLayout);
      //TODO maybe we want to put this on the purchase button isntead
      populatePackLayout(lockedPacks, paidPackLayout);
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
    
    Button btn = (Button) this.findViewById(R.id.PackPurchase_Button_Next);
    btn.setOnClickListener(mGameSetupListener);
    
  }

  /**
   * Remove from lockedPacks those packs that are already installed.
   * @param lockedPacks
   * @param localPacks
   * @return
   */
  private LinkedList<Pack> removeLocalPacks(LinkedList<Pack> lockedPacks, LinkedList<Pack> localPacks) {
    Log.d(TAG, "removeLocalPacks");
    for (Pack localPack : localPacks) {
      for (int lockedIndex=0; lockedIndex<lockedPacks.size(); ++lockedIndex) {
        if (localPack.getId() == lockedPacks.get(lockedIndex).getId()) {
          lockedPacks.remove(lockedIndex);
        }
      }
    }
    
    return lockedPacks;
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

      // Create a new row for this pack
      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.packpurchaserow, layout);
      PackPurchaseRowLayout row = (PackPurchaseRowLayout) line
          .getChildAt(count);
      
      // Assign the pack to the row. This should maybe be done in
      // a constructor
      row.setPack(curPack, getPackPref(curPack), count % 2 == 0);

      // Add pack rows to the list. Give margin so borders don't double up.
      LinearLayout.LayoutParams margin = (LinearLayout.LayoutParams) row
          .getLayoutParams();
      final float DENSITY = this.getResources().getDisplayMetrics().density;
      if(count > 0)
      {
        margin.setMargins(0, (int) (-2 * DENSITY), 0, 0);

      }
      row.setLayoutParams(margin);
      mPackLineList.add(row);

      // Set listeners for the row's click events
      row.setOnPackSelectedListener(mSelectPackListener);
      row.setOnPackInfoRequestedListener(mPackInfoListener);
      
      // Add the current pack object to the row so that the listener can get its
      // metadata
      row.setTag(curPack);
      
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

    // ToDo: Get these getting the correct packs.
    // Is this the best way to get installed packs?
    LinkedList<Pack> localPacks = new LinkedList<Pack>();
    localPacks = game.getInstalledPacks();

    int numEasyPhrases = 0;
    int numMediumPhrases = 0;
    int numHardPhrases = 0;
    int totalPhrases = 0;
    for (int i = 0; i < localPacks.size(); i++) {
      if (getPackPref(localPacks.get(i))) {
        // numSelected += localPacks.get(i).getNumPlayablePhrases();
        numEasyPhrases += 200+(i*200);
        numMediumPhrases += 400;
        numHardPhrases += 100;
      } 
    }
    totalPhrases = numEasyPhrases + numMediumPhrases + numHardPhrases;

    if(totalPhrases == 0)
    {   
      // Set bar title
      bar.setTitle("No Phrases Selected!");
      
      int rowColor = this.getResources().getColor(R.color.packPurchaseNoneSelected);
      int labelColor = this.getResources().getColor(R.color.white);
      bar.setSegmentComponents(0, 0, "No Phrases Selected!", rowColor, labelColor);
      bar.setSegmentComponents(1, 0, "", rowColor, labelColor);
      bar.setSegmentComponents(2, 0, "", rowColor, labelColor);
    }
    else
    {
      // Set bar title
      bar.setTitle("Selected Phrases: " + Integer.toString(totalPhrases));

      totalPhrases = numEasyPhrases + numMediumPhrases + numHardPhrases;
      int rowAndLabelColor = this.getResources().getColor(R.color.packInfo_EasyPhrases);
      bar.setSegmentComponents(0, numEasyPhrases, "Easy", rowAndLabelColor, rowAndLabelColor);
      rowAndLabelColor = this.getResources().getColor(R.color.packInfo_MediumPhrases);
      bar.setSegmentComponents(1, numMediumPhrases, "Medium", rowAndLabelColor, rowAndLabelColor);
      rowAndLabelColor = this.getResources().getColor(R.color.packInfo_HardPhrases);
      bar.setSegmentComponents(2, numHardPhrases, "Hard", rowAndLabelColor, rowAndLabelColor);
    }
    
    // ReRender bar
    bar.updateSegmentWeights();
  }
  
  
  /*
   * Helper function to install a purchased pack
   */
  private void installPack(Pack packToInstall)
  {
    // TODO: Catch the runtime exception
    try {
      new PackInstaller().execute(packToInstall);
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
    
    showToast(packToInstall.getName());
    if (getPackPref(packToInstall)) {
      setPackPref(packToInstall, false);
    } else {
      setPackPref(packToInstall, true);
    }
  }
  
  /**
   * The market sends us the Product ID of a purchased item.  With that we 
   * can infer which pack the user is requesting and get it from the server.
   * @param id The pack Id of the pack that should be installed.
   */
  private void installPack(int id) {
    for (Pack curPack : mPayPacks) {
      if (curPack.getId() == id) {
        // TODO: Catch the runtime exception correctly
        try {
          installPack(curPack);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
      }
    }
  }
  
  // TODO: DEBUG CODE, THIS SHOULD NOT GO TO PRODUCTION
  /**
   * The market sends us the Product ID of a purchased item.  With that we 
   * can infer which pack the user is requesting and get it from the server.
   * @param id The pack Id of the pack that should be installed.
   */
  private void installPack(String name) {
    for (Pack curPack : mPayPacks) {
      if (curPack.getName().equals(name)) {
        // TODO: Catch the runtime exception correctly
        try {
          installPack(curPack);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * The market sends us the Product ID of a purchased item.  With that we 
   * can infer which pack the user is requesting and get it from the server.
   * @param id The pack Id of the pack that should be removed if possible.
   */
  private void uninstallPack(int id) {
    // TODO: Catch the runtime exception correctly
    try {
      new PackUninstaller().execute(id);
    } catch (RuntimeException e) {
        e.printStackTrace();
    }
  }
  
  // TODO: DEBUG CODE, THIS SHOULD NOT GO TO PRODUCTION
  /**
   * The market sends us the Product ID of a purchased item.  With that we 
   * can infer which pack the user is requesting and get it from the server.
   * THIS METHOD DOES NOT NEED TO EXIST AS IN THE FUTURE ID IS ALL WE NEED
   * THE ONLY REASON WE HAVE THIS IS BECAUSE OF THE android.test PACKAGES
   * @param id The pack Id of the pack that should be installed.
   */
  private void uninstallPack(String name) {
    for (Pack curPack : mPayPacks) {
      if (curPack.getName().equals(name)) {    
        //TODO: Catch the runtime exception correctly
        try {
          new PackUninstaller().execute(curPack.getId());
        } catch (RuntimeException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Opens the twitter client for promotional packs
   */
  private void openTwitterClient()
  {
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
      startActivityForResult(shareIntent, TWITTER_REQUEST_CODE);
    } else {
      showToast(getString(R.string.toast_packpurchase_notwitter));
    }
  };

  /**
   * Opens the Facebook client for promotional packs
   */
  private void openFacebookClient()
  {
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
  };

  /**
   * Opens the Google client for promotional packs
   */
  private void openGoogleClient()
  {
    ComponentName targetComponent = getClientComponentName(mFoundGoogleClients);

    // TODO intent is a stupid name
    if (targetComponent != null) {
      Intent gplusIntent = new Intent(Intent.ACTION_SEND);
      gplusIntent.setComponent(targetComponent);
      String intentType = ("text/plain");
      gplusIntent.setType(intentType);
      gplusIntent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT SUBJECT" + "\n"
          + "TESTING");
      gplusIntent
          .putExtra(Intent.EXTRA_TEXT,
              "TESTING TESTING \n https://market.android.com/details?id=com.buzzwords");
      startActivityForResult(gplusIntent, GOOGLEPLUS_REQUEST_CODE);
    } else {
      showToast(getString(R.string.toast_packpurchase_nogoogleplus));
    }
  };

  /**
   * Listen for the result of social activities like twitter, facebook, and
   * google+
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "****** ACTIVITY RESULT RESULTCODE = " + resultCode);

    if(requestCode == PACKINFO_REQUEST_CODE)
    {
      if(resultCode == RESULT_CANCELED)
      {
        // Do nothing
        return;
      }
        
      // Get the pack
      Pack curPack = (Pack) data.getExtras().get(getString(R.string.packBundleKey));
      
      switch(resultCode)
      {
        case PackInfo.RESULT_OK:
          purchasePack(curPack);
          break;
        case PackInfo.RESULT_TWITTER:
          openTwitterClient();
          // TODO: This should not occur until they RETURN from the Tweet
          //   but we would have trouble getting to the Pack they just tweeted ABOUT
          // returning from Tweet etc.
          installPack(curPack);
          break;
        case PackInfo.RESULT_FACEBOOK:
          openFacebookClient();
          // TODO: This should not occur until they RETURN
          installPack(curPack);
          break;
        case PackInfo.RESULT_GOOGLE:
          openGoogleClient();
          // TODO: This should not occur until they RETURN
          installPack(curPack);
          break;
      }

    }
    else if(requestCode == TWITTER_REQUEST_CODE)
    {
      // TODO: Here is where we really want to install the pack, but we 
      // can't get to the pack from here...
    }
  }

  /*
   * Helper function to submit the request to the billing service
   */
  private void purchasePack(Pack packToPurchase)
  {  
    //showPackInfo(packToPurchase);
    Log.d(TAG, "purchasePack(" + packToPurchase.getName() + ")");
    // TODO: REMOVE THIS CODE ITS FOR DEBUGGING
    if (packToPurchase.getId() >= 1010 && packToPurchase.getId() <= 1013) {
      mBillingService.requestPurchase(String.valueOf(packToPurchase.getName()), packToPurchase.getName());
    }
    else {
      mBillingService.requestPurchase(String.valueOf(packToPurchase.getId()), packToPurchase.getName());
    }
  }

  /** 
   * Run installations in an Async Task.  This puts the intensive task of installing
   * on a separate thread that once complete will dismiss the progress dialog and refresh
   * the layout.
   */
  private class PackInstaller extends AsyncTask <Pack, Void, String>
  {
      private ProgressDialog dialog;

      @Override
      protected void onPreExecute()
      {
        dialog = ProgressDialog.show(
          PhrasePackPurchase.this,
          null,
          getString(R.string.progressDialog_install_text), 
          true);
      }

      @Override
      protected String doInBackground(Pack... pack)
      {
        GameManager gm = new GameManager(PhrasePackPurchase.this);
        gm.installPack(pack[0]);
        return "";
      }

      @Override
      protected void onPostExecute(String result)
      {
        dialog.dismiss();
        refreshAllPackLayouts();
        findViewById(R.id.PackPurchase_ScrollView).scrollTo(0, 0);
      }
  }
  
  /** 
   * Run uninstalls in an Async Task.  This puts the intensive task of db deletions
   * on a separate thread that once complete will dismiss the progress dialog and refresh
   * the layout.
   */
  private class PackUninstaller extends AsyncTask <Integer, Void, String>
  {
      private ProgressDialog dialog;

      @Override
      protected void onPreExecute()
      {
        dialog = ProgressDialog.show(
          PhrasePackPurchase.this,
          null,
          getString(R.string.progressDialog_uninstall_text), 
          true);
      }

      @Override
      protected String doInBackground(Integer... packIds)
      {
        GameManager gm = new GameManager(PhrasePackPurchase.this);
        gm.uninstallPack(packIds[0]);
        return "";
      }

      @Override
      protected void onPostExecute(String result)
      {
        dialog.dismiss();
        refreshAllPackLayouts();
        findViewById(R.id.PackPurchase_ScrollView).scrollTo(0, 0);
      }
  }
  
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
    intent.putExtra(
        getApplication().getString(R.string.packInfoIsPackPurchased),
        false);//pack.isInstalled());
    // TODO Add PurchaseType to packs to read from server so we don't have
    // to pass it in, or check against ID
    int purchaseType = Pack.PURCHASETYPE_BUY;
    switch(pack.getId())
    {
    case 4:
      purchaseType = Pack.PURCHASETYPE_TWEET;
      break;
    case 5:
      purchaseType = Pack.PURCHASETYPE_FACEBOOK;
      break;
    case 6:
      purchaseType = Pack.PURCHASETYPE_GOOGLE;
      break;
    }
    intent.putExtra("HACK_PurchaseType", purchaseType);
    
    startActivityForResult(intent, PACKINFO_REQUEST_CODE);
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
   * If the database has not been initialized, we send a
   * RESTORE_TRANSACTIONS request to Android Market to get the list of purchased items
   * for this user. This happens if the application has just been installed
   * or the user wiped data. We do not want to do this on every startup, rather, we want to do
   * only when the database needs to be initialized.
   */
  private void restorePacks() {
    Log.d(TAG, "restorePacks");
    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    boolean initialized = prefs.getBoolean(
        Consts.PREFKEY_PACKS_INITIALIZED, false);
    if (!initialized) {
      Log.d(TAG, "restoring transactions...");
      mBillingService.restoreTransactions();
      Toast.makeText(this, R.string.packpurchase_restoring_packs, Toast.LENGTH_LONG).show();
    }
    else {
      Log.d(TAG, "restore not necessary");
    }
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

  /**
   * Called when this activity is no longer visible.
   */
  @Override
  protected void onStop() {
      super.onStop();
      ResponseHandler.unregister(mPurchaseObserver);
  }

  @Override
  protected void onDestroy() {
      super.onDestroy();
      mBillingService.unbind();
  }
}
