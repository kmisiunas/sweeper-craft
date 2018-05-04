package com.misiunas.mcs

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.{Button, RelativeLayout, Toast}
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.games.Games
import com.misiunas.mcs.game.GameActivity
import com.misiunas.mcs.highscore.GoogleSignIn
import java.util.Locale

import com.google.android.gms.ads.{AdRequest, AdView, MobileAds}
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.minor.{AdHelper, TrackingHelper}
import com.tooltip.Tooltip
import org.greenrobot.eventbus.{EventBus, Subscribe, ThreadMode};



/** # Entry Activity for the application
  *
  * If the game is started - autostart the game
  *
  * Created by kmisiunas on 2016-08-16.
  */
class MainMenuActivity extends FragmentActivity {

  protected var tracker: Tracker = null
  protected var gameHelper: GoogleSignIn = null
  private var continueButton: Button = null
  private var tooltip: Tooltip = null

  // ======= INITIALISATION =======

  override def onCreate(savedInstanceState: Bundle): Unit = {
    loadLanguage() // change language
    super.onCreate(savedInstanceState)
    val extras = getIntent().getExtras()
    tracker = this.getApplication.asInstanceOf[SweeperCraftApp].getDefaultTracker
    // force portrait orientation for phones // todo remove?
    val screenSize: Int = getResources.getConfiguration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK
    if (screenSize != Configuration.SCREENLAYOUT_SIZE_LARGE && screenSize != Configuration.SCREENLAYOUT_SIZE_XLARGE) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    gameHelper = this.getApplication.asInstanceOf[SweeperCraftApp].getGameHelper(this)
    // Display xml (after language selection)
    setUpViews()
    // manage default behavior
    SaveState.isThereASave(this) match {
      case true if extras == null => // no parameters passed
        continueButton.setEnabled(true)
        startActivity(new Intent(this, classOf[GameActivity]))
      case true if extras.getBoolean("autoplay", true) => // todo does not work?
        continueButton.setEnabled(true)
        startActivity(new Intent(this, classOf[GameActivity]))
      case true => // no autoplay
        continueButton.setEnabled(true)
      case false =>
        continueButton.setEnabled(false)
    }

    AdHelper.prepare(
      findViewById(R.id.adView).asInstanceOf[AdView],
      findViewById(R.id.linearLayoutAd),
      getApplicationContext()   )

  }


  private def setUpViews(): Unit = {
    setContentView(R.layout.main_menu)
    continueButton = findViewById(R.id.button_continue).asInstanceOf[Button]
    initialiseButtonHandlers()
  }

  override def onResume() {
    super.onResume()
    gameHelper.onStart(this)
    EventBus.getDefault().register(this); // todo move onStart as suggested? Here to avoid multiple bus tigers
    if (SaveState.isThereASave(this)) continueButton.setEnabled(true)
    else continueButton.setEnabled(false)
    TrackingHelper.reportTexture(tracker, this)
    TrackingHelper.reportZoom(tracker, this)
    updateHighscoreTooltip()
  }

  override protected def onStop() {
    super.onStop()
    gameHelper.onStop()
  }

  override protected def onPause() {
    super.onPause()
    EventBus.getDefault().unregister(this)
    if(tooltip != null && tooltip.isShowing) tooltip.dismiss()
  }

  // ======= HANDLE EVENTS ======

  override def onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    setUpViews() // to do not sure if this does anything
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    gameHelper.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 1) {
      Log.d("MainMenu", "Reloading screen")
      reload()
    }
  }

  /** initialise the button handlers */
  private def initialiseButtonHandlers(): Unit = {
    /** Continue button */
    continueButton.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        startActivity(new Intent(view.getContext, classOf[GameActivity]))
      }
    })

    /** New Game button
      * In a single view just start new activity.
      * In a multi-fragment tablet landscape add new fragment. */
    val newgame: Button = findViewById(R.id.button_new_game).asInstanceOf[Button]
    newgame.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        val fragmentHolder: View = findViewById(R.id.second_fragment_holder)
        if (true || fragmentHolder == null) {
          // no fragment holder means portrait orientation
          Log.d("MainMenu", "Starting activity to show new game creation fragmen")
          startActivity(new Intent(view.getContext, classOf[NewGameActivity]))
        }
        else {
          //dual fragment view
          Log.d("MainMenu", "Extending main layout to include new game fragment.")
          var fragment: NewGameFragment = getSupportFragmentManager.findFragmentById(R.id.second_fragment_holder).asInstanceOf[NewGameFragment]
          if (fragment == null || fragmentHolder.getVisibility == View.GONE) {
            fragment = new NewGameFragment
            val ft: FragmentTransaction = getSupportFragmentManager.beginTransaction
            ft.replace(R.id.second_fragment_holder, fragment)
            ft.setTransition(FragmentTransaction.TRANSIT_NONE)
            ft.addToBackStack(null)
            ft.commit
            fragmentHolder.setVisibility(View.VISIBLE)
          }
        }
      }
    })

    /** Settings button */
    val settings: Button = findViewById(R.id.button_settings).asInstanceOf[Button]
    settings.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        startActivityForResult(new Intent(view.getContext, classOf[SettingsActivity]), 1)
      }
    })

    /** Highscore button */
    val highscore: Button = findViewById(R.id.button_score).asInstanceOf[Button]
    val activity = this
    highscore.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View): Unit = {
        gameHelper.performActionOrLogin(
          activity,
          startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(gameHelper.getApiClient), 2),
          updateHighscoreTooltip()
        )
      }
    })
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  def onEvent(event: GoogleSignIn.SignInFailed): Unit = {
    updateHighscoreTooltip()
    Toast.makeText(this, gameHelper.getSignInError.getActivityResultCode, Toast.LENGTH_SHORT).show()
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  def onEvent(event: GoogleSignIn.SignInSucceeded): Unit = {
    if(tooltip != null)  tooltip.dismiss()
  }

  // ====== Helper methods ======

  /** small box below Highscore button */
  private def showHighscoreTooltip(resId: Int): Unit = {
    val highscore: Button = findViewById(R.id.button_score).asInstanceOf[Button]
    if(tooltip != null && tooltip.isShowing) tooltip.dismiss()
    tooltip = new Tooltip.Builder(highscore, R.style.tooltip)
      .setText( getResources.getText(resId).toString )
      .build()
    tooltip.show()
  }

  /** Update tooltip*/
  private def updateHighscoreTooltip(): Unit ={
    if (!GoogleSignIn.isNetworkAvailable(this)){
      showHighscoreTooltip(R.string.highscores_go_online)
    } else if (!gameHelper.isSignedInSafe()){
      showHighscoreTooltip(R.string.highscores_sign_in)
      Log.d("Tooltip", "Sign in tooltip triggered")
    } else if( tooltip != null ){
      tooltip.dismiss()
    }
  }



  /** Method for loading language */
  private def loadLanguage(): Unit = {
    val myLocale: Locale = Settings.language(this)
    Log.d("Language", "interpreted=" + myLocale.getLanguage)
    val res: Resources = getResources
    val dm: DisplayMetrics = res.getDisplayMetrics
    val conf: Configuration = res.getConfiguration
    conf.locale = myLocale
    res.updateConfiguration(conf, dm)
  }

  override def onBackPressed() {
    val fragment_holder: View = findViewById(R.id.second_fragment_holder)
    if (fragment_holder != null) fragment_holder.setVisibility(View.GONE)
    super.onBackPressed()
  }

  /** clean way to reload this activity */
  private def reload() {
    val refresh: Intent = new Intent(this, classOf[MainMenuActivity])
    refresh.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    refresh.putExtra("autoplay", false)
    startActivity(refresh)
    finish()
  }

}