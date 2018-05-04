package com.misiunas.mcs.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.misiunas.mcs.game.{GameState, Pos, PosFloat}
import com.misiunas.mcs.{R, Settings}

/** # Main drawing method for the game
  *
  * ## To Do
  *  - this class was written long time ago and could use some abstraction
  *
  * ## Notes:
  *  - For performance we can leave it as a single class
  *
  * ## Position tracking math
  *
  * Draw big canvas map with size ~= 3*width  x 3*heigth
  * Start drawing at offset() that captures the position variations
  * To get the sceen position consider that we get
  *
  * Converted from Java
  * Created by kmisiunas on 2016-07-04.
  */


// Last modified on 24/20/2011
class GridView(
                val context: Context,
                val width: Int,
                val height: Int,
                val gameState: GameState ) extends View(context) {

  // ------- Initialization --------

  var zoom: Int =  Settings.zoom(context)

  /**  textures to draw */
  var textures: Array[Bitmap] = prepareTextures()

  var drawGrid: Boolean = Settings.drawGrid(context)
  val gridPaint: Paint = new Paint()
  gridPaint.setColor(Color.BLACK)


  /** the square where rendering begins */
  private var firstSquare: Pos = Pos(0,0)

  /** corner to start rendering the map */
  private var corner: Pos = Pos(0, 0)

  /** buffer image that is stored so far */
  private var buffer: Array[Array[Bitmap]] = Array.ofDim[Bitmap](width / zoom * 3 + 1, height / zoom * 3 + 1)

  /** selection image - not a smart implementation as it keeps loading from memory */
  private var selection: Option[BitmapDrawable] = None


  updateWholeImage()



  // ------- Public Methods --------

  override protected def onDraw(canvas: Canvas): Unit = {
    for (i <- buffer.indices; j <- buffer.head.indices) {
      canvas.drawBitmap(buffer(i)(j), i * zoom + offset.x, j * zoom + offset.y, null)
      if (drawGrid){
        var x = offset.x + i * zoom
        var y = offset.y + j * zoom
        canvas.drawLine(x, y, x + zoom, y, gridPaint)
        canvas.drawLine(x, y, x, y + zoom, gridPaint)
      }
    }

    selection match {
      case Some(box) => box.draw(canvas)
      case None => {}
    }

    // testing code
//    testCross match {
//      case Some(p) =>
//        canvas.drawLine(p.x, 0, p.x , 3*width, gridPaint)
//        canvas.drawLine(0, p.y, 3*height , p.y, gridPaint)
//      case None => {}
//    }
  }

  /** Method for updating image that is rendered */
  def updateWholeImage(): Unit =  {
    corner = (gameState.position * zoom).toPos - (Pos(width, height) * 3 / 2)
    firstSquare = corner / zoom
    updateBuffer(firstSquare)
  }



  def setSelection(pos: Pos): Unit = {
    if (pos.x >= 0 && pos.x < gameState.size.size.x && pos.y >= 0 && pos.y < gameState.size.size.y) {
      val box = ResourcesCompat.getDrawable(getResources(), R.drawable.selection_texture, null).asInstanceOf[BitmapDrawable]
      val selectionBorder: Int = 10 // todo customisable? for other games maybe
      val zoomScale: Float = (zoom * 1.0 / (box.getIntrinsicHeight - selectionBorder * 2)).toFloat
      val location = (pos - firstSquare) * zoom + offset // left top
      box.setFilterBitmap(false)
      box.setBounds(
        location.x - (selectionBorder * zoomScale).toInt,
        location.y - (selectionBorder * zoomScale).toInt,
        location.x + (selectionBorder * zoomScale).toInt + zoom,
        location.y + (selectionBorder * zoomScale).toInt + zoom
      )
      selection = Some(box)
      this.invalidate
    }
  }

  /** Estimates what square is drawn on the given screen position - only static situations */
  def getSquareNumber(pos: Pos): Pos =  (translate(pos) - offset) / zoom + firstSquare

  /** method needed for live zoom */
  def reinitialise(): Unit = {
    zoom =  Settings.zoom(context)
    textures = prepareTextures()
    drawGrid = Settings.drawGrid(context)
    buffer = Array.ofDim[Bitmap](width / zoom * 3 + 1, height / zoom * 3 + 1)
  }

  // ------- Important private  methods -------

  private def prepareTextures(): Array[Bitmap] = {
    val options = new BitmapFactory.Options
    options.inScaled = false
    val textureId = Settings.themeTexture(context)
    val large: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(this.getResources, textureId, options),
        zoom * 6,
        zoom * 8,
        false    )

    /** function for slicing the texture file. It must have 6 columns */
    def cutMap(id: Int): Bitmap = {
      val size: Int = large.getWidth / 6
      return Bitmap.createBitmap(large, size * (id % 6), size * (id / 6), size, size)
    }
    // create the array
    (0 until (6*8)).map(cutMap).toArray
  }

  /** finds offset within one square, for continuous scrolling */
  private def offset: Pos = ( corner / zoom ) * zoom - corner + Pos(0,1)*zoom

  /** translates screen position to the scroll screen position */
  private def translate(pos: Pos): Pos = Pos(pos.x + width, pos.y + height) //- Pos(0, 76)
  // todo number accounts for correction in vertical direction (calibration)






  // ------- Dull tasks -------

  private def updateBuffer(at: Pos): Unit = {
    for (i <- buffer.indices; j <- buffer.head.indices) {
      buffer(i)(j) = textures(gameState.getImage( Pos(at.x + i, at.y + j) ))
    }
  }


  /** remove selection drawing */
  def disableSelection(): Unit = { selection = None }

  override def scrollBy(dx: Int, dy: Int) {
    val oldPos = (gameState.position * zoom).toPos
    val posX: Int = Math.max(Math.min(oldPos.x + dx, gameState.size.size.x * zoom + 200 - width / 2), width / 2 - 200)
    val posY: Int = Math.max(Math.min(oldPos.y + dy, (gameState.size.size.y + 1) * zoom + 350 - height / 2), height / 2 - 350)
    gameState.position = PosFloat( posX*1.0f/zoom , posY*1.0f/zoom)
    updateWholeImage
  }

  // ----- test functions -------

  def setTestCross(x: Int, y: Int): Unit = { testCross = Some( translate(Pos(x, y)) ) }
  private var testCross: Option[Pos] = None
  // rest is in calibrationview


}
