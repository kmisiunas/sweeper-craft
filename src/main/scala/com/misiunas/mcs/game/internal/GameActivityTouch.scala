package com.misiunas.mcs.game.internal

import android.app.Activity
import android.os.Bundle
import android.view.{MotionEvent, ScaleGestureDetector}
import com.misiunas.mcs.Settings
import com.misiunas.mcs.game.Pos

abstract class GameActivityTouch extends com.misiunas.mcs.game.internal.GameActivityButtons {
  private var currentX: Int = 0
  private var currentY: Int = 0
  private var initialX: Int = 0
  private var initialY: Int = 0
  private var thereWasDrag: Boolean = false
  private var dragTolerance: Int = 11
  private var calibrationY: Int = 0
  private var multiTouch: Boolean = false
  private var timeFirstTouch: Long = 0L
  private var scaleDetector: ScaleGestureDetector = null

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    calibrationY = com.misiunas.mcs.Settings.touchCalibration(this).y
    estimateDragTolerance
    scaleDetector = new ScaleGestureDetector(this, new ScaleListener(this))
  }

  /** todo handle pinch https://developer.android.com/training/gestures/scale.html
    * about handling multiple fingers https://developer.android.com/training/gestures/multi.html
    */
  override def onTouchEvent(event: MotionEvent): Boolean = {
    val posOfView = new Array[Int](2)
    container.getLocationOnScreen( posOfView )

    def x: Int = event.getX.toInt - posOfView(0) // adjust for screen elements
    def y: Int = event.getY.toInt - posOfView(1) + calibrationY

    event.getAction match {

      case MotionEvent.ACTION_UP if multiTouch =>
        multiTouch = false
        gridView.scrollBy(initialX - currentX, initialY - currentY)
        container.scrollBy(-initialX + currentX, -initialY + currentY)
        updateView()

      case MotionEvent.ACTION_UP if thereWasDrag =>
        gridView.scrollBy(initialX - currentX, initialY - currentY)
        container.scrollBy(-initialX + currentX, -initialY + currentY)
        updateView()

      case MotionEvent.ACTION_UP =>
        gridView.disableSelection
        clickActionHappened( gridView.getSquareNumber( Pos(initialX, initialY) ) )

      case _ if multiTouch =>
        scaleDetector.onTouchEvent(event)

      case _ if event.getPointerCount()>1 && timeSinceFirstTouch < 200 =>
        multiTouch = true
        gridView.disableSelection
        scaleDetector.onTouchEvent(event)

      case MotionEvent.ACTION_DOWN =>
        timeFirstTouch  = System.currentTimeMillis()
        currentX = x
        currentY = y
        initialX = currentX
        initialY = currentY
        gridView.setSelection( gridView.getSquareNumber( Pos(initialX,initialY) ))
        gridView.invalidate
        thereWasDrag = false
        //gridView.setTestCross(currentX,currentY) //  testing

      case MotionEvent.ACTION_MOVE if thereWasDrag =>
        val x2: Int = x
        val y2: Int = y
        gridView.disableSelection
        container.scrollBy(currentX - x2, currentY - y2)
        currentX = x2
        currentY = y2

      case MotionEvent.ACTION_MOVE if !thereWasDrag => // todo improve!
        val deviation: Double =
          (x - initialX) * (x - initialX) + (y- initialY) * (y - initialY)
        if (deviation >= dragTolerance * dragTolerance) thereWasDrag = true

      case _ => {} // don't care
    }
    return super.onTouchEvent(event)
  }

  private def timeSinceFirstTouch: Long = System.currentTimeMillis() - timeFirstTouch

  override protected def onResume {
    super.onResume
    calibrationY = com.misiunas.mcs.Settings.touchCalibration(this).y
  }

  protected def clickActionHappened(pos: Pos): Unit = {
    if ( !operator.isGameOver ) {
      vibrate
      operator.clickAction(pos, tool)
      updateView()
      updateInfo
      clickActionHappenedOther()
    } else {
      updateView()
    }
  }

  def clickActionHappenedOther(): Unit // game related stuff in GameActivity

  def estimateDragTolerance: Unit = { // todo poor implementation
    dragTolerance = 10 + ( Settings.zoom(this) / 4)
  }


  /** Handle zoom in zoom out events
    */
  class ScaleListener(context: Activity) extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    var oldZoom: Int = 100
    private var scaleFactor = 1.0f

    override def onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean = {
      oldZoom = Settings.zoom(context)
      scaleFactor = 1.0f
      return true
    }

    override def onScale(detector: ScaleGestureDetector): Boolean = {
      scaleFactor *= detector.getScaleFactor()
      Settings.setZoomClosest( (oldZoom*scaleFactor).toInt , context)
      gridView.reinitialise()
      updateView()
      return true;
    }

  }

}