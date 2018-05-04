package com.misiunas.mcs.minor

import android.content.Context
import android.net.{ConnectivityManager, NetworkInfo}
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import com.google.android.gms.ads.{AdRequest, AdView, MobileAds}
import com.misiunas.mcs.R

/** Other Ads are placed directly into GameServicsActivity
  *
  * Created by kmisiunas on 2016-08-30.
  */
object AdHelper {

  private var ad :AdRequest = null

  /**
    * @param adView pass findViewById(R.id.adView).asInstanceOf[AdView]
    * @param context pass getApplicationContext() from activity
    */
  def prepare(adView: AdView, adHolder: View, context: Context): Unit = {
    MobileAds.initialize(context, "ca-app-pub-5454624221267779~4928479448")
    if (shouldHideAds(context)) {
      adHolder.setVisibility(View.GONE)
    } else {
      adHolder.setVisibility(View.VISIBLE)
      if(ad == null) ad = new AdRequest.Builder().build()
      adView.loadAd( ad )
    }
  }

  /** checks if internet is available and if users are not annoyed */
  private def shouldHideAds(context: Context): Boolean = {
//    Log.d("first_open", ""+ firstDay(context) )
    return true // disabled since i don't like these banners
    !isNetworkAvailable(context) || firstDay(context)
  }

  private def firstDay(context: Context) : Boolean = {
    val first = PreferenceManager.getDefaultSharedPreferences(context).getLong("mcs.first_open", 0L)
    Log.d("first_open", first.toString)
    if(first == 0L) { // not set
      val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
      editor.putLong("mcs.first_open", System.currentTimeMillis() )
      editor.commit()
      true
    } else {
      first <= System.currentTimeMillis() && System.currentTimeMillis() <= first + 12*60*60*1000
    }
  }

  private def isNetworkAvailable(context: Context): Boolean = {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val activeNetworkInfo: NetworkInfo = connectivityManager.getActiveNetworkInfo()
    activeNetworkInfo != null && activeNetworkInfo.isConnected()
  }

}
