package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.{Operator, Pos, Tool}

/**
  * Class that represents all the tiles
  * Created by kmisiunas on 2016-07-01.
  */
abstract class Tile {

  // # Need specific implementations

  def getImage: Int

  def clickAction(action: Action): Unit

  def openAction(action: Action): Unit

  // # with universal implementations

  def setHidden(texture: Int): Tile = texture match {
    case 0 => HiddenStone(this.baseTile)
    case 1 => HiddenDirt(this.baseTile)
    case 2 => HiddenGravel(this.baseTile)
    case _ => HiddenStone(this.baseTile)
  }


  def setFlag: Tile = this match {
    case tile: HiddenTile => FlagTile(tile)
    case _ => this
  }

  def setProtected: Tile = this match {
    case tile: HiddenTile => ProtectedTile(tile)
    case _ => this
  }

  def revealTile: Tile = this match {
    case t: HiddenTile => t.tile
    case t: FlagTile => t.tile.tile
    case t: ProtectedTile => t.tile.tile
    case _ => this
  }

  def removeMarkings: Tile = this match {
    case t: FlagTile => t.tile
    case t: ProtectedTile => t.tile
    case _ => this
  }

  def baseTile: Tile = this.removeMarkings.revealTile

  // # Other interpretation methods

  def isHidden: Boolean = false

  def isActive: Boolean = true

}

abstract class Action {def tool: Tool; def pos: Pos; def operator: Operator }
object Action {def apply(tool: Tool, pos: Pos, operator: Operator) = ActionUser(tool, pos, operator) }

// Method for representing user action on tiles
case class ActionUser(val tool: Tool, val pos: Pos, val operator: Operator) extends Action

case class ActionAutomatic(val tool: Tool, val pos: Pos, val operator: Operator) extends Action
