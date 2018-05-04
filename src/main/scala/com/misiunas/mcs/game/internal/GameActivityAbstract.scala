package com.misiunas.mcs.game.internal

import java.util.Locale

import android.app.Activity
import android.content.res.{Configuration, Resources}
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.{DisplayMetrics, Log}
import android.view.{Display, View, ViewTreeObserver}
import android.widget.RelativeLayout
import com.misiunas.mcs._
import com.misiunas.mcs.draw.GridView
import com.misiunas.mcs.game.{GameState, Operator, Pos, StateCreator}

abstract class GameActivityAbstract extends Activity {

  protected var gridView: GridView = null
  protected var operator: Operator = null
  protected var container: RelativeLayout = null

  // ------- Activity Stuff -------

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    loadLanguage()
    setContentView(R.layout.game)
    operator = Operator( SaveState.get(this) )
    container = findViewById(R.id.container).asInstanceOf[RelativeLayout]
    val activity = this
    container.getViewTreeObserver().addOnGlobalLayoutListener(
      new ViewTreeObserver.OnGlobalLayoutListener() {
        override def onGlobalLayout(): Unit =  {
          container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          val windowSize: Pos = Pos(container.getWidth, container.getHeight)
          Log.d("Container window size", windowSize.toString)
          gridView = new GridView(activity, windowSize.x, windowSize.y, operator)
          val layoutParams: RelativeLayout.LayoutParams =
            new RelativeLayout.LayoutParams(AndroidBS.WRAP_CONTENT, AndroidBS.WRAP_CONTENT)
          layoutParams.setMargins(-windowSize.x, -windowSize.y, -windowSize.x, -windowSize.y) // container size offset
          container.addView(gridView, layoutParams)
      }
    });

//    val windowSize: Pos = Pos(container.getMeasuredWidth, container.getMeasuredHeight)
//    Log.d("Container window size", windowSize.toString)
//    gridView = new GridView(this, windowSize.x, windowSize.y, operator)
//    val layoutParams: RelativeLayout.LayoutParams =
//      new RelativeLayout.LayoutParams(AndroidBS.WRAP_CONTENT, AndroidBS.WRAP_CONTENT)
//    layoutParams.setMargins(-windowSize.x, -windowSize.y, -windowSize.x, -windowSize.y) // container size offset
//    container.addView(gridView, layoutParams)
  }


  override protected def onPause(): Unit =  {
    super.onPause
    SaveState.save(operator, this)
  }

  override protected def onResume(): Unit = {
    super.onResume()
    // todo invalidate gridView to reset zoom and textures
  }


  private def loadLanguage(): Unit = {
    val myLocale: Locale = Settings.language(this)
    Log.d("Language", "interpreted=" + myLocale.getLanguage)
    val res: Resources = getResources
    val dm: DisplayMetrics = res.getDisplayMetrics
    val conf: Configuration = res.getConfiguration
    conf.locale = myLocale
    res.updateConfiguration(conf, dm)
  }


  // ------- Promise ------

  protected def loadViewAndResources

  protected def updateLeftCorner

  def putGameOverMessage(title: Int, text: Int)

  protected def estimateDragTolerance(): Unit


}

