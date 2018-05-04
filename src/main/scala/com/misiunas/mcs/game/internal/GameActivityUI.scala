package com.misiunas.mcs.game.internal

import android.graphics.drawable.BitmapDrawable
import android.os.{Build, Bundle}
import android.preference.PreferenceManager
import android.widget.{ImageView, TextView}
import com.misiunas.mcs.R
import com.misiunas.mcs.game.config.GameMode
import com.misiunas.mcs.game.tiles.{Creeper, Enderman, Zombie}

abstract class GameActivityUI extends com.misiunas.mcs.game.internal.GameActivityAbstract {

  final private val lifeViews: Array[ImageView] = Array.ofDim[ImageView](4)

  private var scoreView: TextView = null;

  protected var axeDrawable: BitmapDrawable = null
  protected var tntDrawable: BitmapDrawable = null
  protected var swordDrawable: BitmapDrawable = null
  private var timeTracking: Long = 0L

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    scoreView = findViewById(R.id.scoreView).asInstanceOf[TextView]
    initScoreView
    timeTracking = System.currentTimeMillis
    loadViewAndResources
  }

  override protected def onResume {
    super.onResume
    timeTracking = System.currentTimeMillis
  }

  def updateGameTime {
    if (!operator.isGameOver) {
      operator.addTime(System.currentTimeMillis - timeTracking)
      timeTracking = System.currentTimeMillis
    }
  }

  protected def updateInfo: Unit = {
    updateScore
    updateLeftCorner
  }

  protected def updateScore: Unit  = {
    scoreView.setText("" + operator.getScore())
    if(operator.mode == GameMode.Rescue){
      if(operator.getScore() < 100) setScoreAppearance(R.style.score_view_warning)
      else setScoreAppearance(R.style.score_view_normal)
    }
  }


  protected def initScoreView = { }

  def updateView(): Unit = {
    gridView.updateWholeImage()
    gridView.invalidate()
  }


  protected def loadViewAndResources(): Unit = {
    lifeViews(0) = (findViewById(R.id.imageLife1).asInstanceOf[ImageView])
    lifeViews(1) = (findViewById(R.id.imageLife2).asInstanceOf[ImageView])
    lifeViews(2) = (findViewById(R.id.imageLife3).asInstanceOf[ImageView])
    lifeViews(3) = (findViewById(R.id.imageLife4).asInstanceOf[ImageView])
    swordDrawable = getResources.getDrawable(R.drawable.tool_weapon).asInstanceOf[BitmapDrawable]
    tntDrawable = getResources.getDrawable(R.drawable.tool_tnt).asInstanceOf[BitmapDrawable]
    axeDrawable = getResources.getDrawable(R.drawable.tool_mine).asInstanceOf[BitmapDrawable]
  }

  protected def updateLeftCorner(): Unit = {
    for( i <- lifeViews.indices ){
      if( i < operator.getLives ){
        lifeViews(i).setBackgroundResource(R.drawable.heart_full)
      } else {
        lifeViews(i).setBackgroundResource(R.drawable.heart_empty)
      }
    }
  }

  // ========  Helpers ==========

  private def setScoreAppearance(resId: Int): Unit = {
    if (Build.VERSION.SDK_INT < 23) {
      scoreView.setTextAppearance(getApplicationContext(), resId)
    } else {
      scoreView.setTextAppearance(resId)
    }
  }


}