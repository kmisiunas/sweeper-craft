package com.misiunas.mcs.game
/**
  * Store all game data
  *
  * Note that Char implementation is internal and rather ugly. It should be optimised for performance
  *
  * Created by kmisiunas on 2016-06-15.
  */
import java.io.Serializable
import java.util

import android.util.Log
import com.misiunas.mcs.game.config._

import scala.collection.immutable.Stream.Empty
import com.misiunas.mcs.game.tiles.{HiddenTile, MonsterTile, Tile}

@SerialVersionUID(1L)
class GameState(
                 val mode: GameMode,
                 val size: GameSize,
                 val difficulty: GameDifficulty
               ) extends Serializable {

  // ------ Additional variables -------

  var position: PosFloat = size.size.toPosFloat / 2 // relative position!

  private val map: Array[Char] = Array.ofDim[Char](size.size.x * size.size.y)

  protected var lastTile: Pos = Pos(-1,-1)
  protected var action: List[String] = List("first_dig") // how to react for the rest of the application

  protected var time: Long = 0L
  protected var lives: Int = mode.initialLives
  protected var resources: Int = 0
  protected var explosives: Boolean = false
  protected var score: Int = mode.initialScore
  protected var reportedGameOver: Boolean = false


  /** key access method */
  def get(pos: Pos): Tile = GameState.mapping( getCharKey(pos) )

  /** quick access to the images for the tile */
  def getImage(pos: Pos): Int = {
    if (pos.y == 0 && (pos.x < -1 || pos.x > size.size.x))
      43
    else if (pos.y == 0 && get(pos).isInstanceOf[HiddenTile])
      42  // special treatment for top row -> show grass
    else
      GameState.mappingImages( getCharKey(pos) )
  }

  /** key changing method */
  def set(rule: (Pos , Tile) ): Boolean = {
    val pos: Pos = rule._1
    if (pos.x >= 0 && pos.y >= 0 && pos.x < size.size.x && pos.y < size.size.y) {
      map(pos.x + pos.y*size.size.y) = GameState.mapTilesToChar(rule._2)
      lastTile = pos
      true
    } else {
      false // can't change that!
    }
  }

  /** mapper: make sure it is fast */
  private def getCharKey(pos: Pos): Char = {
    if( pos.x>=0 && pos.y>=0 && pos.x<size.size.x && pos.y<size.size.y) map(pos.x + pos.y*size.size.y)
    // Far off map
    else if(pos.x < -1 && pos.y < 0) 228
    else if(pos.x >= -1 && pos.x <= size.size.x && pos.y < -1) 229
    else if(pos.x > size.size.x  && pos.y < 0) 230
    else if(pos.x > size.size.x  && pos.y >= 0 && pos.y < size.size.y) 231
    else if(pos.x > size.size.x  && pos.y >= size.size.y) 232
    else if(pos.x >= -1 && pos.x <= size.size.x  && pos.y > size.size.y) 233
    else if(pos.x < -1  && pos.y >= size.size.y) 234
    else if(pos.x < -1 && pos.y >= 0 && pos.y < size.size.y) 235
    // Close off map
    else if(pos.x == -1 && pos.y == -1) 220
    else if(pos.x >= 0 && pos.x < size.size.x && pos.y == -1) 221
    else if(pos.x == size.size.x && pos.y == -1) 222
    else if(pos.x == size.size.x && pos.y >= 0 && pos.y < size.size.y) 223
    else if(pos.x == size.size.x &&  pos.y == size.size.y) 224
    else if(pos.x >= 0 && pos.x < size.size.x && pos.y == size.size.y) 225
    else if(pos.x == -1 &&  pos.y == size.size.y) 226
    else if(pos.x == -1 && pos.y >= 0 && pos.y < size.size.y) 227
    else 255 // error
  }


  // SPECIAL ACTIONS

  /** method here to improve efficiency */
  protected def checkForVictory: Boolean = map.forall( !GameState.isHiddenSafeTile(_) )

  /** evaluate this function once to maximise efficiency */
  private def isHiddenSafeTile2(c: Char): Boolean = {
    val monsters: Set[Char] =
      (18 to 216).map(_.toChar)
        .filter(i => GameState.mapping(i).revealTile.isInstanceOf[MonsterTile])
        .toSet
    //    hidden tile           but not monster
    (c >= 18.toChar && c < 216.toChar)  && !monsters(c)
  }

  /** custom hash implementation for checking if the code was modified offline */
  override def hashCode(): Int = {
    var res = 7
    val prime = 79
    res = res * prime + util.Arrays.hashCode(map)
    if(action.nonEmpty) {
      //res = res * prime + action.map(_.hashCode).sum // causes problems
    }
    res = res * prime + (time ^ (time >>> 32)).toInt
    res = res * prime + lives
    res = res * prime + resources
    res = res * prime + (if (explosives) 1 else 0)
    res = res * prime + (if (explosives) 1 else 0)
    res = res * prime + score
    res = res * prime + (if (reportedGameOver) 1 else 0)
    res
  }

}




object GameState {


  import tiles._

  private def mapping(key: Char): Tile = key match {
    case 0 => EmptyTile
    case 1 => Number(1)
    case 2 => Number(2)
    case 3 => Number(3)
    case 4 => Number(4)
    case 5 => Number(5)
    case 6 => Number(6)
    case 7 => Number(7)
    case 8 => Number(8)
    case 9 => Goal
    case 10 => Zombie
    case 11 => Creeper
    case 12 => Enderman
    case 13 => Pie
    case 14 => Steak
    case 15 => GoldOre
    case 16 => DiamondOre
    case 17 => Chest
    // leaving 4 empty slots

    // hidden fields
    case x if 22 <= x && x < 22*2 => mapping((x - 22).toChar).setHidden(0)
    case x if 22*2 <= x && x < 22*3 => mapping((x - 22*2).toChar).setHidden(1)
    case x if 22*3 <= x && x < 22*4 => mapping((x - 22*3).toChar).setHidden(2)
    // Other cases - special markers
    case x if 22*4 <= x && x < (22*4*7/4) => mapping((x - 22*3).toChar).setFlag
    case x if 22*4*7/4 <= x && x < (22*4*10/4) => mapping((x - 22*3*2).toChar).setProtected

    // from the end - special tiles
    case 255 => ErrorTile
    case 254 => StartingPoint
    case 243 => Explosives
    case 242 => Sword
    //sides
    case 220 => Sides(1)
    case 221 => Sides(2)
    case 222 => Sides(3)
    case 223 => Sides(4)
    case 224 => Sides(5)
    case 225 => Sides(6)
    case 226 => Sides(7)
    case 227 => Sides(8)
    case 228 => FarSides(1)
    case 229 => FarSides(2)
    case 230 => FarSides(3)
    case 231 => FarSides(4)
    case 232 => FarSides(5)
    case 233 => FarSides(6)
    case 234 => FarSides(7)
    case 235 => FarSides(8)
    // else
    case _ => ErrorTile
  }

  private lazy val mappingImages: Map[Char, Int] = {
    Log.d("GameState", "lazy first evaluation of mappingImages map" )
    (0 to 255).map(i => (i.toChar -> mapping(i.toChar).getImage ) ).toMap
  }

  private lazy val mapTilesToChar: Map[Tile, Char] = (0 to 255).map(i => ( mapping(i.toChar) -> i.toChar)).toMap

  /** evaluate this function once to maximise efficiency */
  private lazy val isHiddenSafeTile: Char => Boolean = {
    val monsters: Set[Char] =
      (18 to 216).map(_.toChar)
        .filter(i => GameState.mapping(i).revealTile.isInstanceOf[MonsterTile])
        .toSet
    //    hidden tile           but not monster
    c => (c >= 18.toChar && c < 216.toChar) && !monsters(c)
  }

}