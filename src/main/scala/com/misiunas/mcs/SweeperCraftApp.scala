package com.misiunas.mcs

import android.app.{Activity, Application}
import android.util.Log
import com.google.android.gms.analytics.{GoogleAnalytics, Tracker}
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.basegameutils.GameHelper.GameHelperListener
import com.misiunas.mcs.highscore.GoogleSignIn
import com.misiunas.mcs.highscore.GoogleSignIn.{SignInFailed, SignInSucceeded}
import org.greenrobot.eventbus.EventBus

/** This is Application extension adapted to maintain app-wide state of particular services.
  * Google Play services depend on the state persisted in this object.
  * This should be a singleton class.
  *
  * Created by kmisiunas on 2016-08-14.
  */
class SweeperCraftApp extends Application {

  /* Singleton instances */
  private var gameHelper: GoogleSignIn = null
  private var tracker: Tracker = null

  def getGameHelper(activity: Activity): GoogleSignIn = {
    if (gameHelper == null) { // setup
      Log.d("GameHelper", "Setting up GameHelper")
      gameHelper = new GoogleSignIn(activity, GameHelper.CLIENT_GAMES)
      gameHelper.enableDebugLog(true) //todo v1 change before release

      gameHelper.setup( new GameHelperListener() {
        override def onSignInFailed(): Unit = EventBus.getDefault().post(SignInFailed)
        override def onSignInSucceeded(): Unit = EventBus.getDefault().post(SignInSucceeded)
      })

    }
    return gameHelper
  }


  /**
    * Gets the default {@link Tracker} for this {@link Application}.
    *
    * @return tracker
    */
  def getDefaultTracker: Tracker = {
    if (tracker == null) {
      val analytics: GoogleAnalytics = GoogleAnalytics.getInstance(this)
      // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
      tracker = analytics.newTracker(R.xml.global_tracker)
    }
    tracker
  }

  override def onTerminate() {
    super.onTerminate()
    if (gameHelper != null) {  gameHelper.onStop()  }
  }


}
