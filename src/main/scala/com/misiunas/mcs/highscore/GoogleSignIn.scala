package com.misiunas.mcs.highscore

import android.app.{Activity, AlertDialog, Dialog, DialogFragment}
import android.content.{Context, DialogInterface}
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import com.misiunas.mcs.R
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.highscore.GoogleSignIn.LoginStatus

/** # Class to extend google login services
  *
  * Google provides checklist for implementation:
  * https://developers.google.com/games/services/checklist
  *
  * Created by kmisiunas on 2016-08-12.
  */
class GoogleSignIn(protected var activity: Activity, var clientsToUse: Int) extends GameHelper (activity, clientsToUse) {

  import GoogleSignIn._

  def canSignIn(): Boolean = { isNetworkAvailable(activity) }

  def isSignedInSafe(): Boolean = this.isSignedIn || this.isConnecting



  def status(): LoginStatus = (isNetworkAvailable(activity), this.isSignedIn()) match {
    case (true, true) => Online_And_Connected
    case (true, false) => Online_And_Disconnected
    case (false, true) => Offline_And_Connected
    case (false, false) => Offline_And_Disconnected
  }

  def performActionOrLogin(activity: Activity, action: => Unit, cantAction: => Unit): Unit ={
    // todo test the behaviour before settling on a solution
    status() match {
      case Online_And_Connected => action
      case Offline_And_Connected => action
      case Online_And_Disconnected =>
        (new ConfirmLoginDialog()).show(activity.getFragmentManager(), "sign in?")
      case Offline_And_Disconnected => cantAction
    }
  }

  /** dialog to login */
  class ConfirmLoginDialog extends DialogFragment {

    private val dialog = this

    override def onCreateDialog(savedInstanceState: Bundle): Dialog =  {
      // Use the Builder class for convenient dialog construction
      val builder = new AlertDialog.Builder( getActivity() )
      builder.setMessage(R.string.google_services_sign_in)
        .setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, id: Int) = {
            // FIRE ZE MISSILES!
            beginUserInitiatedSignIn()
          }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, id: Int) {
            // User cancelled the dialog
            dialog.dismiss()
          }
        })
      // Create the AlertDialog object and return it
      return builder.create()
    }

  }

}




object GoogleSignIn {

  // todo useless
  trait LoginStatus
  case object Offline_And_Disconnected extends LoginStatus
  case object Offline_And_Connected extends LoginStatus
  case object Online_And_Disconnected extends LoginStatus
  case object Online_And_Connected extends LoginStatus

  abstract class LoginEvent
  case class SignInSucceeded() extends LoginEvent
  case class SignInFailed() extends LoginEvent


  /** checks if network connection is available */
  def isNetworkAvailable(context: Context): Boolean = {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
      .asInstanceOf[ConnectivityManager]
    val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()
    return activeNetworkInfo != null && activeNetworkInfo.isConnected()
  }
}

