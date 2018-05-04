package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.Tool.{Flag, Mine, Protect}

/**
  * Representation of hidden tiles
  *
  * Created by kmisiunas on 2016-07-01.
  */
abstract class HiddenTile (val tile: Tile) extends Tile {

  override def isHidden: Boolean = true

  def openAction(action: Action): Unit = {}

  def clickAction(action: Action): Unit = action.tool match {
    case Mine =>
      if( action.operator.hasSword  && action.isInstanceOf[ActionUser]){
        action.operator.removeSword()
        this.tile match {
          case Zombie =>
            removeMonster(action)
            action.operator.addAction("diamond_sword_kill_zombie")
          case Enderman =>
            removeMonster(action)
            action.operator.addAction("diamond_sword_kill_enderman")
          case Creeper =>
            removeMonster(action)
            action.operator.addAction("diamond_sword_kill_creeper")
            action.operator.addExplosives()
          case _ =>
            action.operator.addAction("diamond_sword_mine_rock")
            action.operator.set(action.pos -> this.tile )
            tile.openAction(action)
        }
      } else { // no sword - just normal open
        action.operator.set(action.pos -> this.tile )
        tile.openAction(action)
      }
    case Flag =>
      action.operator.set(action.pos -> this.setFlag )
    case Protect =>
      action.operator.set(action.pos -> this.setProtected )
  }

  private def removeMonster(action: Action): Unit = {
    action.operator.cleanPos(action.pos)
  }

}

// Implementations

case class HiddenStone(override val tile: Tile) extends HiddenTile(tile) { override def getImage: Int = 9 }
case class HiddenDirt(override val tile: Tile) extends HiddenTile(tile){  override def getImage: Int = 12 }
case class HiddenGravel(override val tile: Tile) extends HiddenTile(tile) { override def getImage: Int = 15 }