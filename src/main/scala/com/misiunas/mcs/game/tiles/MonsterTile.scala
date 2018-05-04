package com.misiunas.mcs.game.tiles


/**
  * Created by kmisiunas on 2016-07-01.
  */

abstract class MonsterTile extends Tile {

  def takeLives: Int

  def clickAction(action: Action): Unit = {}

  def openAction(action: Action): Unit = {
    action.operator.get(action.pos).revealTile match {
      case Zombie => action.operator.addAction("found_zombie")
      case Enderman => action.operator.addAction("found_enderman")
      case Creeper => action.operator.addAction("found_creeper")
    }
    action.operator.addLives(-takeLives)
  }

}

object Zombie extends MonsterTile { override def takeLives: Int = 1;  override def getImage: Int = 24 }
object Creeper extends MonsterTile { override def takeLives: Int = 2;  override def getImage: Int = 25 }
object Enderman extends MonsterTile  { override def takeLives: Int = 3;  override def getImage: Int = 26 }