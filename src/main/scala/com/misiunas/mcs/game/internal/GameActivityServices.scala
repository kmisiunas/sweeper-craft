package com.misiunas.mcs.game.internal

import android.content.{Context, Intent}
import android.net.{ConnectivityManager, NetworkInfo}
import android.os.{Bundle, Handler}
import com.google.android.gms.ads.{AdListener, AdRequest, InterstitialAd}
import com.google.android.gms.analytics.Tracker
import com.misiunas.mcs.SweeperCraftApp
import com.misiunas.mcs.badges.AchievementInspector
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.highscore.GoogleSignIn
import com.misiunas.mcs.minor.RateApp
import org.greenrobot.eventbus.{EventBus, Subscribe, ThreadMode}

abstract class GameActivityServices extends com.misiunas.mcs.game.internal.GameActivityUI {

  final private val PERIODIC_INTERVAL: Int = 500 //ms
  private var timedHandler: Handler = null
  protected var badges: AchievementInspector = null // todo replace with modern version
  private var doPeriodicUpdates: Boolean = true
  protected var tracker: Tracker = null
  protected var gameHelper: GameHelper = null
  protected var interstitialAd: InterstitialAd = null
  private var startTime: Long = 0L


  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    tracker = this.getApplication.asInstanceOf[SweeperCraftApp].getDefaultTracker
    badges = AchievementInspector(this)
    timedHandler = new Handler
    // RateApp(this).addCheck(timedHandler, this) // not needed for now
    gameHelper = this.getApplication.asInstanceOf[SweeperCraftApp].getGameHelper(this)
    // Ad
    interstitialAd = new InterstitialAd(this)
    interstitialAd.setAdUnitId("ca-app-pub-5454624221267779/6470857447")
    interstitialAd.setAdListener(new AdListener() {
      override def onAdClosed() = { requestNewInterstitialAd() }
    })
    requestNewInterstitialAd()
    startTime = operator.getTime
  }

  protected def requestNewInterstitialAd(): Unit = {
    val adRequest: AdRequest = new AdRequest.Builder()
      .addTestDevice("A681B1C8C310315463DEE3034A99F769") // nexus 4
      .build();
    interstitialAd.loadAd(adRequest);
  }

  protected def considerShowingAd(): Unit = {
    if(interstitialAd.isLoaded() && operator.getTime > startTime+ 2*60*1000) interstitialAd.show()
  }

  protected def stopPeriodicUpdates() = {doPeriodicUpdates = false}

  protected def periodicUpdates: Unit = {
    updateGameTime
    updateScore
    if (badges.hasNewBadges) badges.showPendingBadge(this, gameHelper)
  }


  override protected def onResume = {
    super.onResume
    gameHelper.onStart(this)
    EventBus.getDefault().register(this)
    timedHandler.postDelayed(periodicTask, PERIODIC_INTERVAL)
  }

  override protected def onPause() = {
    super.onPause
    EventBus.getDefault().unregister(this)
    timedHandler.removeCallbacks(periodicTask)
    badges.saveState(this)
    //gameHelper.onStop()
  }


  override protected def onDestroy() = {
    super.onDestroy
    timedHandler.removeCallbacks(periodicTask)
  }


  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = {
    super.onActivityResult(requestCode, resultCode, data)
    gameHelper.onActivityResult(requestCode, resultCode, data)
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  def onEvent(event: GoogleSignIn.SignInFailed): Unit = {
    // todo
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  def onEvent(event: GoogleSignIn.SignInSucceeded): Unit = {
    // todo
  }


  final private val periodicTask: Runnable = new Runnable() {
    def run {
      periodicUpdates
      if( doPeriodicUpdates ) timedHandler.postDelayed(this, PERIODIC_INTERVAL)
    }
  }

}