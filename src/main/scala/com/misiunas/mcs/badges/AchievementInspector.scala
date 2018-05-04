package com.misiunas.mcs.badges

import java.io._

import android.app.Activity
import android.content.{Context, Intent}
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.games.Games
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.{NewGameActivity, Settings}
import com.misiunas.mcs.game.{GameActivity, Operator}
import com.misiunas.mcs.highscore.GoogleSignIn$

/** Principles:
  *  - check method are not allowed to edit external states
  *
  * Maybe add separate thread
  *
  * Created by kmisiunas on 2016-07-22.
  */
class AchievementInspector (private var unlocked: Set[Badge],
                            private var locked: Set[Badge]    ) extends Serializable {


  // Public methods

  def saveState(activity: Activity): Unit = {
    try {
      val fos: FileOutputStream = activity.openFileOutput("AchievementList", Context.MODE_PRIVATE)
      val out: ObjectOutputStream = new ObjectOutputStream(fos)
      out.writeObject(this)
      out.close()
      fos.close()
    } catch {
      case ex: IOException => Log.e("BadgesInspector", "Could not save the achievements", ex)
    }
  }

  def checkMilestones(gs: Operator): Boolean = checkAction("", gs)

  def checkAction(action: String, gs: Operator): Boolean = {
    val added = locked.filter(_.checkIfAchieved(action, gs))
    unlocked = unlocked ++ added
    locked = locked -- added
    pendingShow = (pendingShow ::: added.toList)
    Log.d("Badge", "pendingShow.length="+pendingShow.length)
    Log.d("Badge", "added.size="+added.size)
    Log.d("Badge", "unlocked.size="+unlocked.size)
    Log.d("Badge", "locked.size="+locked.size)
    if(added.size >= 1) Log.d("Badge", "added="+added.head )
    added.nonEmpty
  }

  def hasNewBadges: Boolean = pendingShow.nonEmpty

  def showPendingBadge(activity: Activity, googleLogin: GameHelper): Unit = if (hasNewBadges) {
    val badge = pendingShow.head
    pendingShow = pendingShow.tail    // remove from pending list
    // report to Google
    try {
      Games.Achievements.unlock(googleLogin.getApiClient, "my_achievement_id")
    } catch {
      case e => Log.e("GoogleLogin", "Could not register achievement with google", e)
    }
    // show UI
    if (Settings.showBadges(activity)) { // can hide badges
      val myIntent: Intent = new Intent(activity, classOf[ShowBadgeActivity])
      myIntent.putExtra(ShowBadgeActivity.KEY_BADGE, Badge.ui(badge) )
      activity.startActivity(myIntent)
    }
  }

  def appendNewBadges(all: Set[Badge]): AchievementInspector = {
    val old = unlocked ++ locked
    val added = all -- old
    locked = locked ++ added
    Log.d("Badge", "(all badges).size="+all.size)
    Log.d("Badge", "(in system badges).size="+(locked ++ unlocked).size)
    this
  }

  // # Private Implementations

  private var pendingShow: List[Badge] = Nil

}


object AchievementInspector {

  // todo: load list from online on a separate thread
  // load state if present
  def apply(activity: Activity): AchievementInspector = {
    val savedState: Option[AchievementInspector] = try {
      val fis: FileInputStream = activity.openFileInput("AchievementList")
      val in: ObjectInputStream = new ObjectInputStream(fis)
      val read = in.readObject
      in.close()
      fis.close()
      Some( read.asInstanceOf[AchievementInspector] )
    } catch {
      case ex: Exception => None
    }
    // check if loading was successful
    savedState match {
      case Some(list) => list.appendNewBadges(Badge.all)
      case None => new AchievementInspector(Set(), Badge.all)
    }
  }


}
