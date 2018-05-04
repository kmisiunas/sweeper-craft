package com.misiunas.mcs.game.internal

import android.os.Bundle
import android.view.View
import android.widget.{TextView, Toast}
import com.misiunas.mcs.R
import com.misiunas.mcs.game.tiles.{Enderman, Zombie}
import com.misiunas.mcs.minor.TutorialActivity

abstract class GameActivitySocial extends com.misiunas.mcs.game.internal.GameActivityTouch {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    if (!TutorialActivity.wasItShow(this)) {
      TutorialActivity.setShown(this)
      TutorialActivity.show(this, operator.mode)
    }
  }

  def putGameOverMessage(title: Int, text: Int) = {
    val activity = this
    activity.runOnUiThread(new Runnable() {
      def run() = {
        activity.findViewById(R.id.textFinalMessage).asInstanceOf[TextView].setText(activity.getString(title))
        activity.findViewById(R.id.textFinalMessage2).asInstanceOf[TextView].setText(activity.getString(text))
        activity.findViewById(R.id.linearLayoutMessage).setVisibility(View.VISIBLE)
      }
    })
  }

  def diamondOreInfo {
    var text: String = this.getString(R.string.diamond_sword_mine_rock)
    if (operator.getLastTile.isInstanceOf[Zombie.type]) text = this.getString(R.string.diamond_sword_kill_zombie)
    else if (operator.getLastTile.isInstanceOf[Enderman.type]) text = this.getString(R.string.diamond_sword_kill_enderman)
    Toast.makeText(getApplicationContext, text, Toast.LENGTH_SHORT).show
  }
}