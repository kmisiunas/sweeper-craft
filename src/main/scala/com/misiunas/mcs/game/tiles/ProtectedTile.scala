package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.Tool.{Flag, Mine, Protect}

/**
  * Created by kmisiunas on 2016-07-01.
  */
case class ProtectedTile (val tile: HiddenTile) extends Tile {

  override def isHidden: Boolean = true

  override def getImage: Int = tile.getImage + 2

  def openAction(action: Action): Unit = {}

  def clickAction(action: Action): Unit = action.tool match {
    case Mine => ()
    case Flag => ()
    case Protect => action.operator.set(action.pos -> tile )
  }

}
