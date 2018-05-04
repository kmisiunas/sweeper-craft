package com.misiunas.mcs.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.analytics.HitBuilders
import com.misiunas.mcs.R
import com.misiunas.mcs.game.config.GameMode
import com.misiunas.mcs.game.internal.GameActivitySocial
import com.misiunas.mcs.game.tiles.{Creeper, Enderman, Zombie}
import com.misiunas.mcs.highscore.Leaderboard
import com.misiunas.mcs.minor.{TrackingHelper, TutorialActivity}

/** # Main Activity for the game
  * Most of the inner workings are hidden in the abstract super-classes
  *
  * Here we focus on interaction with the game, ie handling the actions in GameState via Operator
  * Policy:
  * 1) Send action to BadgesInspector
  * 2) If untriggered, give toast with appropriate message
  *
  */
class GameActivity extends GameActivitySocial {

  override protected def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    updateInfo // todo ???
    clickActionHappenedOther()
  }


  override def clickActionHappenedOther(): Unit = {
    new Thread(new Runnable() { // new thread to avoid blocking main thread
      def run(): Unit = {
        operator.checkIfWon()
        badges.checkMilestones(operator)
        processAction()
      }
    }).start()
  }



  /** linear action processor */
  def processAction(): Unit = {
    val action = operator.processNextAction()
    action match {
      case None => {}
      case Some(msg) if msg != "won" && msg != "lost" && msg != "first_dig" =>
        updateOnAction(msg)
        executeAction(msg)
        processAction()
      case Some(msg) =>
        updateOnAction(msg)
        executeAction(msg)
    }
  }

  private def executeAction(action: String): Unit = action match {
    case "first_dig" => {}
    case "lost" =>
      declareLost()
      processOnceGameOver()
      stopPeriodicUpdates()
    case "won" =>
      declareVictory()
      processOnceGameOver()
      stopPeriodicUpdates()
    case st: String if badges.checkAction(st, operator) => {} // do nothing first time - wait for badge
    case "found_gold" => // boring because it is common
    case "found_diamond" =>
      // todo report on sword progress
      // create action "made_sword"
    case st: String => toastMessage(st)
  }

  private def updateOnAction(action: String): Unit = action match {
    case "diamond_sword_kill_creeper" => updateButtonsGraphics()
    case "diamond_sword_mine_rock" => updateButtonsGraphics()
    case "found_diamond" => updateButtonsGraphics()
    case "explosives_boom" => updateButtonsGraphics()
    case "found_sword" => updateButtonsGraphics()
    case "diamond_sword_kill_zombie" => updateButtonsGraphics()
    case "diamond_sword_kill_enderman" => updateButtonsGraphics()
    case "show_tutorial" => showTutorial(this)
    case "found_zombie" => vibrateLong
    case "found_enderman" => vibrateLong
    case "found_creeper" => vibrateLong
    case _ => {}
  }

  protected def showTutorial(activity: Activity) = {
    val activity = this
    activity.runOnUiThread(new Runnable() {
      def run() = TutorialActivity.show(activity, operator.mode)
    })
  }


  def toastMessage(id: String): Unit = {
    try {
      val message = getResources().getString( getResources().getIdentifier(id, "string", getPackageName()) )
      val activity = this
      activity.runOnUiThread(new Runnable() {
        def run() = {
          Toast.makeText(activity, message, Toast.LENGTH_SHORT).show() // todo conflicts with the new thread
        }
      })
    } catch {
      case _: Throwable => Log.d("GameActivity", "Resource " + id + " not found")
    }
  }

  // ===== Once in a cycle event  reactions ====

  private def processOnceGameOver() = if(!operator.haveReportedGameOver) {
    operator.setReportedGameOver
    if (operator.isGameWon){
      TrackingHelper.reportVictory(tracker, operator)
    } else {
      TrackingHelper.reportLost(tracker, operator)
    }
    Leaderboard.add(this, gameHelper, operator)
  }





  // ===== Messaging implementations ======

  private def declareVictory(): Unit = {
    operator.mode match {
      case GameMode.Adventure => putGameOverMessage(R.string.won_adventure, R.string.won_adventure_extended)
      case GameMode.Rescue => putGameOverMessage(R.string.won_rescue, R.string.won_rescue_extended)
      case GameMode.Classic => putGameOverMessage(R.string.won_classic, R.string.won_classic_extended)
    }
  }

  private def declareLost(): Unit = {
    if (operator.getScore() < 0) putGameOverMessage(R.string.lost_out_of_time, R.string.lost_out_of_time_extended)
    else if (Math.random < 0.1)  putGameOverMessage(R.string.lost_died, R.string.lost_died_rabbit)
    else operator.getLastTile match {
      case Zombie => putGameOverMessage(R.string.lost_died, R.string.lost_died_zombie)
      case Creeper => putGameOverMessage(R.string.lost_died, R.string.lost_died_creeper)
      case Enderman => putGameOverMessage(R.string.lost_died, R.string.lost_died_enderman)
      case _ =>
        Log.e("game", "not supposed to have other death cases")
        putGameOverMessage(R.string.lost_died, R.string.lost_died_rabbit)
    }
  }


}

