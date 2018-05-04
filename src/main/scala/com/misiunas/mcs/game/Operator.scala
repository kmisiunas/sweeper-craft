package com.misiunas.mcs.game

import android.util.Log
import com.misiunas.mcs.game.GameState
import com.misiunas.mcs.game.Tool.Mine
import com.misiunas.mcs.game.config.{GameDifficulty, GameMode, GameSize}
import com.misiunas.mcs.game.tiles.{Explosives, _}

/**
  * Acts on the game
  *
  * Created by kmisiunas on 2016-06-16.
  */
@SerialVersionUID(1L)
class Operator ( override val mode: GameMode,
                 override val size: GameSize,
                 override val difficulty: GameDifficulty
               ) extends GameState( mode, size, difficulty ) {



  private val gs: GameState = this


  // ---- Main Interaction Methods  ----

  /** returns true if action was accepted */
  def clickAction(pos: Pos, tool: Tool): Boolean = {
    if (isGameOver) false
    else if (pos.x < 0 || pos.y < 0 || pos.x >= size.size.x || pos.y >= size.size.y) false // outside
    else { // game is on!
      if (isItFirstDig) doFirstDig(pos)
      else gs.get(pos).clickAction( Action(tool, pos, this) )
      true
    }
  }


  def checkIfWon(): Boolean = {
    if(this.checkForVictory){
      addAction("uncovered_all_tiles")
      addAction("won")
      true
    } else {
      false
    }
  }




  // ------ Special Actions ------

  private def doFirstDig(pos: Pos): Unit = {
    pos.nineBlock.foreach(moveMonster)
    pos.nineBlock.foreach(moveMonster) // make sure they are gone
    pos.nineBlock.foreach(moveMonster) // double sure
    gs.get(pos).clickAction( Action(Mine, pos, this) )
    gs.set(pos -> StartingPoint)
    if(mode == GameMode.Rescue){ // place Goal tile
      def placeGoal(): Unit = {
        val rnd = Pos.random( size.size )
        if(rnd.distance(pos) < 20) placeGoal() // to close
        else {
          gs.set(rnd -> mimickTile(Goal, gs.get(rnd)) )
          normaliseCountAround(pos)
        }
      }
      placeGoal()
    }
    removeAction("first_dig")
  }

  /** move monster somewhere else */
  private def moveMonster(pos: Pos) {
    if( isMonster(pos) ){ // only work if there is monster here
      val rnd = Pos.random(size.size)
      val oldTile = gs.get(rnd)
      oldTile.revealTile match {
        case e: Number => // move monster without a trace!
          gs.set(rnd -> mimickTile(gs.get(pos).revealTile, oldTile))
          gs.set(pos -> mimickTile(oldTile, gs.get(pos)))
          normaliseCountAround(rnd)
          normaliseCountAround(pos)
        case _ => moveMonster(pos) // try again
      }
    }
  }




  // -------- Control methods ------

  /** methid for removin any special tiles and leaves tile open */
  def cleanPos(pos: Pos): Unit = {
    this.set(pos -> EmptyTile)
    pos.nineBlock.foreach( this.normaliseCount(_) )
  }


  /** normalises the tiles and leaves them as they were found */
  def normaliseCount(pos: Pos): Unit = gs.get(pos).revealTile match {
    case tile: Number =>
      val count = countMonstersAround(pos)
      if(tile.i != count) {
        gs.set(pos -> mimickTile(
          Number(countMonstersAround(pos)),
          gs.get(pos)
        ))
      }
    case _ => Unit // do nothing
  }

  def normaliseCountAround(pos: Pos): Unit = pos.nineBlock.foreach(normaliseCount)


  private def mimickTile(tileNew: Tile, original: Tile): Tile = {
    val res: Tile = original.removeMarkings match {
      case h: HiddenGravel => h.copy( tile = tileNew.baseTile )
      case h: HiddenDirt => h.copy( tile = tileNew.baseTile )
      case h: HiddenStone => h.copy( tile = tileNew.baseTile )
      case _ => tileNew.baseTile
    }
    original match {
      case _: FlagTile => res.setFlag
      case _: ProtectedTile => res.setProtected
      case _ => res
    }
  }

  // todo need zoom to operate this
  def centerViewOn(pos: Pos): Unit = {position = PosFloat(0.5f, 0.5f) + pos}



  // -------- Know all methods ------

  def isMonster(pos: Pos): Boolean = get(pos).revealTile match {
    case _: MonsterTile => true
    case _ => false
  }

  def countMonstersAround(pos: Pos): Int = pos.around.count(isMonster)

  // -------- Actions -------

  def addAction(msg: String): Unit = {
    Log.d("Operator", "Action added. All actions: "+ this.action)
    this.action = this.action :+ msg
  }

  def removeAction(msg: String): Boolean = {
    if(msg == "lost" || msg == "won") false // not allowed to remove lost
    else if( this.action.contains(msg) ) {
      this.action = this.action.filter(_ != msg)
      true
    } else false
  }

  def processNextAction(): Option[String] = this.action.nonEmpty match {
    case true =>
      val next = this.action.head
      if( next != "won" && next != "lost" && next != "first_dig") this.action = this.action.tail
      Some(next)
    case false => None
  }

  def actionContains(msg: String): Boolean = this.action.contains(msg)


  // -------- Boring access methods ------


  def addScore(i: Int): Unit = { this.score = score + i }
  def getScore(): Int = {
    val total = this.score - mode.timeScorePenalty(time)
    if(total < 0) addAction("lost") // for exploration mode must find it before time runs out!
    total
  }

  def addTime(dt: Long): Long = { this.time += dt; getTime }
  def getTime: Long = this.time

  def addLives(number: Int) = {
    this.lives = math.min(lives + number, 4)
    if(!isAlive) { addAction("died"); addAction("lost") }
  }

  def addDiamond(): Unit = {this.resources = this.resources + 1 }
  def addSword(): Unit = {this.resources = 2}
  def removeSword(): Unit = {this.resources = 0}
  def hasSword: Boolean = resources >= 2

  def addExplosives(): Unit = {this.explosives = true}
  def removeExplosives(): Unit = { this.explosives = false }
  def hasExplosives: Boolean = explosives

  def haveReportedGameOver: Boolean = reportedGameOver
  def setReportedGameOver: Unit = {reportedGameOver = true}

  def getLastTile: Tile = gs.get(lastTile)
  def getLastPos: Pos = lastTile

  def isAlive: Boolean = lives > 0
  def getLives: Int = lives

  def isItFirstDig: Boolean = actionContains("first_dig")
  def isGameWon: Boolean  = actionContains("won")
  def isGameLost: Boolean = actionContains("lost")
  def isGameOver: Boolean = isGameWon || isGameLost || !isAlive

}

object Operator {
  def apply(gs: GameState): Operator = gs.asInstanceOf[Operator] // todo might not work at all!
}