package com.misiunas.mcs.game.tiles

/**
  * Created by kmisiunas on 2016-07-01.
  */
abstract class OutsideTiles extends Tile {
  override def isActive: Boolean = false
  def clickAction(action: Action): Unit = {}
  def openAction(action: Action): Unit = {}
}

case class Sides(i: Int) extends OutsideTiles {
  override def getImage: Int = 29+i
}

case class FarSides(i: Int) extends OutsideTiles {
  override def getImage: Int = i match {
    case 1 => 38
    case 2 => 38
    case 3 => 38
    case 4 => 39
    case 5 => 40
    case 6 => 40
    case 7 => 40
    case 8 => 41
    case _ => 47
  }
}