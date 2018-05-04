package com.misiunas.mcs.minor

import android.content.Context
import com.google.android.gms.analytics.{HitBuilders, Tracker}
import com.misiunas.mcs.Settings
import com.misiunas.mcs.game.Operator

/** Tracking optimisation so the ugly code can be hidden away
  * Created by kmisiunas on 2016-08-12.
  */
object TrackingHelper {

  def reportVictory(tracker: Tracker, operator: Operator) = {
    tracker.send(new HitBuilders.EventBuilder()
      .setCategory("Game")
      .setAction("Won")
      .setLabel(""+operator.mode+"; "+operator.size+"; "+operator.difficulty)
      .setValue(1)
      .build()
    )
    tracker.send(new HitBuilders.TimingBuilder()
      .setCategory("Game")
      .setValue(operator.getTime)
      .setVariable("Won Time")
      .setLabel(""+operator.mode+"; "+operator.size+"; "+operator.difficulty)
      .build()
    )
  }


  def reportLost(tracker: Tracker, operator: Operator) = {
    tracker.send(new HitBuilders.EventBuilder()
      .setCategory("Game")
      .setAction("Lost")
      .setLabel(""+operator.mode+"; "+operator.size+"; "+operator.difficulty)
      .setValue(1)
      .build()
    )
    tracker.send(new HitBuilders.TimingBuilder()
      .setCategory("Game")
      .setValue(operator.getTime)
      .setVariable("Lost Time")
      .setLabel(""+operator.mode+"; "+operator.size+"; "+operator.difficulty)
      .build()
    )
  }

  def reportTexture(tracker: Tracker, context: Context): Unit = {
    val name = Settings.themeTextureName(context)
    tracker.send(new HitBuilders.ScreenViewBuilder()
      .setCustomDimension(1, name)
      .build()
    )
  }

  def reportZoom(tracker: Tracker, context: Context): Unit = {
    val zoom = Settings.zoom(context)
    tracker.send(new HitBuilders.ScreenViewBuilder()
      .setCustomDimension(2, zoom.toString)
      .build()
    )
  }


}
