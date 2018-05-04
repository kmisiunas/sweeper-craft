package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.Pos
import com.misiunas.mcs.game.Tool.{Flag, Mine, Protect}

import scala.annotation.tailrec

/**
  * Special note on treasures: they might only be places in empty tiles, thus they are like Empty tile
  *
  * Created by kmisiunas on 2016-07-02.
  */
abstract class TreasureTile extends Number(0) {

  // inherit openAction(action: Action): Unit

  override def clickAction(action: Action): Unit = action.tool match {
    case Protect => {}
    case _ =>
      action.operator.set(action.pos -> EmptyTile)
      treasureAction(action)
  }

  def treasureAction(action: Action): Unit

}

// --------- foods -------------

object Pie extends TreasureTile {
  override def getImage: Int = 22
  override def treasureAction(action: Action): Unit = {
    action.operator.addLives(1)
    action.operator.addAction("ate_pie")
  }
}

object Steak extends TreasureTile {
  override def getImage: Int = 23
  override def treasureAction(action: Action): Unit = {
    action.operator.addLives(3)
    action.operator.addAction("ate_steak")
  }
}


// --------- treasurers --------

object GoldOre extends TreasureTile {
  override def getImage: Int = 18
  override def treasureAction(action: Action): Unit = {
    action.operator.addAction("found_gold")
    action.operator.addScore(4)
  }
}

object DiamondOre extends TreasureTile {
  override def getImage: Int = 19
  override def treasureAction(action: Action): Unit = {
    action.operator.addAction("found_diamond")
    action.operator.addDiamond()
  }
}

object Chest extends TreasureTile {
  override def getImage: Int = 20
  override def treasureAction(action: Action): Unit = {
    val events = List(giveSword, giveGold, giveMap)
    def pick() = scala.util.Random.shuffle(events).head
    pick()(action)
  }

  private def giveSword: Action => Unit = action => {
    action.operator.set( action.pos -> Sword )
    action.operator.addAction("found_treasure_sword")
  }

  /** spread gold in empty tiles around */
  private def giveGold: Action => Unit = action =>  {
    def setEmptyToGold(pos: Pos): Unit = {
      action.operator.get(pos) match {
        case EmptyTile => action.operator.set(pos -> GoldOre)
        case _ => ()
      }
    }
    action.pos.nineBlock.foreach( setEmptyToGold )
    action.operator.addAction("found_treasure_gold")
  }

  /** uncover empty area somewhere in the map and focus on that */
  private def giveMap: Action => Unit = action => {

    def tryFindingEmpty(): Option[Pos] = {
      val pos = Pos.random(action.operator.size.size)
      action.operator.get(pos) match {
        case t: HiddenTile  => t.revealTile match {
          case num: Number if num.i == 0 => Some(pos)
          case _ => None
        }
        case _ => None
      }
    }

    @tailrec
    def findEmptySpot(attempt: Int = 1): Option[Pos] = tryFindingEmpty() match {
      case Some(pos) => Some(pos)
      case None if attempt >= 4000 => None
      case None => findEmptySpot(attempt + 1)
    }

    findEmptySpot() match {
      case None => action.operator.addAction("found_treasure_empty")
      case Some(pos) =>
        action.operator.get(pos).clickAction( ActionAutomatic(Mine, pos, action.operator) )
        action.operator.centerViewOn(pos)
        action.operator.addAction("found_treasure_map")
    }
  }
}