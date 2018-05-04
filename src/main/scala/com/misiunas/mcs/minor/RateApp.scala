package com.misiunas.mcs.minor

import android.app.AlertDialog
import android.content.{Context, DialogInterface, Intent}
import android.net.{ConnectivityManager, NetworkInfo, Uri}
import android.os.Handler
import android.preference.PreferenceManager
import com.misiunas.mcs.R

/** Ask to rate the app:
  * - ask after player played at least 3 days
  * - ask player only if internet is available
  * - ask player only once
  * - ask player only after he player for 3 min
  *
  * Problems: does not stop the time!
  *
  * Created by kmisiunas on 2016-08-13.
  */
class RateApp (toAsk: Boolean) {

  val debug = false

  var connectivityManager: ConnectivityManager = null

  /** add only if there is hope */
  def addCheck(timedHandler: Handler, context: Context): Unit = if(toAsk||debug) {
    connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    timedHandler.postDelayed(checkInFuture(context), 3*60*1000)
  }

  private def checkInFuture(context: Context): Runnable = new Runnable() {
    def run {
      if(isNetworkAvailable||debug) ask(context)
    }
  }

  def isNetworkAvailable: Boolean = connectivityManager.getActiveNetworkInfo != null


  def ask(context: Context): Unit =  {
    PreferenceManager.getDefaultSharedPreferences(context).edit.putBoolean(RateApp.keyToRateApp, false).commit()
    val builder: AlertDialog.Builder = new AlertDialog.Builder(context)
    builder.setIcon(R.drawable.review)
    builder.setTitle(context.getString(R.string.please_review_this_app))
    builder.setMessage(context.getString(R.string.review_this_app_explained))
    builder.setCancelable(false)
    builder.setNegativeButton(context.getString(R.string.button_later), new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
    })
    builder.setPositiveButton(context.getString(R.string.button_review_now), new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, id: Int) {
        // Call market app with this app on it
        val intent: Intent = new Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("market://details?id=com.misiunas.mcs"))
        context.startActivity(intent)
        dialog.cancel()
      }
    })
    builder.create
    builder.show
  }

}



object RateApp {

  private val keyToRateApp = "mcs.ask_to_rate"

  def apply(context: Context): RateApp = {
    val first = PreferenceManager.getDefaultSharedPreferences(context).getLong("mcs.rate_first_game_time", 0L)
    if (first == 0L) {
      PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putLong("mcs.rate_first_game_time", System.currentTimeMillis()).commit()
      return RateApp.apply(context)
    } else {
      val toAsk = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(keyToRateApp, true)
      new RateApp(toAsk && (System.currentTimeMillis() - first) > 3*24*60*60*1000)
    }
  }


}
