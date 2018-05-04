package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.Pos
import com.misiunas.mcs.game.Tool.{Flag, Mine, Protect}

import scala.annotation.tailrec

/**
  * Created by kmisiunas on 2016-07-01.
  */
abstract class Number (val i: Int) extends Tile {

  def getImage: Int = i

  // todo flag action on normal tiles...
  def clickAction(action: Action): Unit = action.tool match {
    case Mine =>
      if(!action.isInstanceOf[ActionAutomatic] && action.operator.hasExplosives){
        action.operator.set(action.pos -> Explosives)
        action.operator.removeExplosives
      }
    case Flag => // auto oppening
      val knownMonsters = action.pos.around.map(pos => action.operator.get(pos)).count(isKnownMonster)
      if(knownMonsters == i) {

        @tailrec
        def openTilesCarefully(list: List[Pos]): Unit = if(list.nonEmpty)  {
          val pos = list.head
          val tile = action.operator.get(pos)
          action.operator.get(pos).clickAction(
            ActionAutomatic(Mine, pos, action.operator)
          )
          list.tail match {
            case _ if (action.operator.isMonster(pos)) =>
              action.operator.addAction("mistake_flag")
              Unit
            case Nil => Unit
            case tail => openTilesCarefully(tail)
          }
        }
        openTilesCarefully(
          action.pos.around.filter(pos => action.operator.get(pos).isInstanceOf[HiddenTile])
        )
      }
      // end of flag tool

    case Protect => ()
  }

  private def isKnownMonster(tile: Tile): Boolean = tile match {
    case _: MonsterTile => true
    case _: FlagTile => true
    case _ => false
  }


  def openAction(action: Action): Unit = {
    action.operator.addScore(1)
    // auto open
    if(i == 0){
      val tiles = action.pos.around
        .map(pos => pos -> action.operator.get(pos).removeMarkings)
        .filter( posTile => posTile._2.isHidden )
      tiles.map(tile => tile._2.clickAction( ActionAutomatic(Mine, tile._1, action.operator) ) )
    }
  }

}

object Number {
  def apply(i: Int): Tile = i match {
    case 0 => EmptyTile
    case 1 => NumberOne
    case 2 => NumberTwo
    case 3 => NumberThree
    case 4 => NumberFour
    case 5 => NumberFive
    case 6 => NumberSix
    case 7 => NumberSeven
    case 8 => NumberEight
    case _ => throw new Exception("Unexpected number while creating Number tile: " + i)
  }

}

object EmptyTile extends Number(0)
object NumberOne extends Number(1)
object NumberTwo extends Number(2)
object NumberThree extends Number(3)
object NumberFour extends Number(4)
object NumberFive extends Number(5)
object NumberSix extends Number(6)
object NumberSeven extends Number(7)
object NumberEight extends Number(8)
