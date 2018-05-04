package com.misiunas.mcs.game

import com.misiunas.mcs.game.config.{GameDifficulty, GameMode, GameSize}
import com.misiunas.mcs.game.tiles._

import scala.annotation.tailrec
import scala.util.Random

/** Class for creating game state
  * Plan:
  * generate map using parameter
  * populate with specified mine difficulty in open format
  * hide all the mines with selected textures
  *
  * Explain operation:
  * object is created and configured. Then it is run, this creates a GameState object.
  *
  *
  *
  */

class StateCreator (mode: GameMode, size: GameSize, difficulty: GameDifficulty) {

  // ==== Main Methods ====
  def create(): GameState = {
    val gs: Operator = new Operator(mode, size, difficulty )
    clearMap(gs)
    populateMines(gs)
    estimateNumbers(gs)
    populateTreasures(gs)
    hideTiles(gs)
    return gs
  }



  // ==== Private Implementations ====

  /** don't need to populate with empty tiles as GameState constructor does it */
  private def clearMap(gs: GameState): Unit = { }


  private def populateMines(gs: GameState) = {
    val noOfMines: Int = (size.size.x * size.size.y * difficulty.mineDensity(mode)).toInt
    /** Method for placing mines => performance bottle-neck? */
    @tailrec
    def placeMineSomewhere(): Unit = {
      val n = Pos.random(size.size)
      gs.get(n) match {
        case EmptyTile =>
          gs.set(n -> Random.shuffle(List(Zombie, Creeper, Enderman)).head )
        case _ =>
          placeMineSomewhere()
      }
    }
    (0 to noOfMines).foreach(i => placeMineSomewhere())
  }

  /** this might be a slow method */
  private def estimateNumbers(operator: Operator): Unit = {
    for (i <- 0 until size.size.x; j <- 0 until size.size.y){
      operator.normaliseCount( Pos(i,j) )
    }
  }

  //todo: fix - there seems to be no other treasure than gold!
  /* Method for populating treasure in the map */
  private def populateTreasures(gs: Operator) = {
    // mine placement engine
    val densities = gs.mode.treasureDensities
    // find all empty fileds
    val empty = Random.shuffle( Pos.range(size.size).filter( gs.get(_) == EmptyTile ) ).toList
    // place mines
    def place( leftRules: Map[TreasureTile, Double], leftPos: List[Pos] ): Unit = {
      if(leftRules.isEmpty || leftPos.isEmpty ) {} else {
        val rule = leftRules.head
        val n = (rule._2 * size.size.x * size.size.y).round.toInt
        leftPos.take(n).map(pos => gs.set(pos -> rule._1))
        return place(leftRules.tail, leftPos.drop(n))
      }
    }
    // do the placement
    place(densities, empty)
  }

  /* Method for hiding the tiles
   *  make it visually nice by using height and clustering*/
  private def hideTiles(gs: GameState) {
    for(i <- 0 until size.size.x; j <- 0 until size.size.y){
      val texture = Math.random match {
        case rnd if rnd < 0.05 => 2 // gravel
        case rnd if rnd < Math.max(0.2, 0.65 - 0.02 * j) => 1 // dirt t higher levels
        case _ => 0 // stone
      }
      gs.set( Pos(i,j) -> gs.get(Pos(i,j)).setHidden(texture) )
    }
  }


}
