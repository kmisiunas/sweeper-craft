package com.misiunas.mcs

import java.util.Locale

import android.content.Context
import android.preference.PreferenceManager
import com.misiunas.mcs.game.{Pos, PosFloat}

/** More on this business https://developer.android.com/guide/topics/ui/settings.html
  *
  * Created by kmisiunas on 2016-07-05.
  */
object Settings {

  // there is another copy in settings.xml
  val zoomLevels = Vector( 48, 52, 58, 64, 72, 80, 88, 96, 106, 116, 128, 140, 156, 174, 192, 212, 232, 256)

  private def zoomLimit(i: Int, fallBack: Int): Int = if(0 <= i && i< zoomLevels.length) i else fallBack

  def zoom(context: Context): Int =
    PreferenceManager.getDefaultSharedPreferences(context).getInt("mcs.zoom", 128)

  def setZoomIn(context: Context): Unit = {
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    val idx = zoomLimit(zoomLevels.indexWhere(_ > zoom(context) ), zoomLevels.length-1)
    editor.putInt("mcs.zoom", zoomLevels(idx) )
    editor.commit()
  }

  def setZoomOut(context: Context): Unit = {
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    val idx = zoomLimit(zoomLevels.lastIndexWhere(_ < zoom(context)) , 0)
    editor.putInt("mcs.zoom", zoomLevels(idx) )
    editor.commit()
  }

  def setZoomClosest(zoom: Int,context: Context): Unit = {
    val dZoom = zoomLevels.map( x => math.abs(x - zoom) )
    val idx = dZoom.indexOf(dZoom.min)
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.putInt("mcs.zoom", zoomLevels(idx) )
    editor.commit()
  }


  def drawGrid(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mcs.settings_show_grid", false)

  def themeTexture(context: Context): Int =
    context.getResources().getIdentifier(themeTextureName(context), "drawable", context.getPackageName())

  def themeTextureName(context: Context): String =
    PreferenceManager.getDefaultSharedPreferences(context).getString("mcs.theme", "texture")

  def vibrate(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mcs.settings_vibrate", true)
  def vibrateDuration(context: Context): Int = 20


  def touchCalibration(context: Context): Pos = Pos(
    PreferenceManager.getDefaultSharedPreferences(context).getInt("mcs.settings_touch_calibration_x", 0) ,
    PreferenceManager.getDefaultSharedPreferences(context).getInt("mcs.settings_touch_calibration_y", 0)
  )

  def setTouchCalibration(context: Context, pos: Pos): Unit = {
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.putInt("mcs.settings_touch_calibration_x", pos.x )
    editor.putInt("mcs.settings_touch_calibration_y", pos.y )
    editor.commit()
  }

  def showBadges(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mcs.settings_show_badges", true)

  def language(context: Context): Locale = {
    val code = PreferenceManager.getDefaultSharedPreferences(context).getString("mcs.settings_language", "Automatic")
    if (code == "Automatic") {
      Locale.getDefault
    } else {
      new Locale(code)
    }
  }

}
