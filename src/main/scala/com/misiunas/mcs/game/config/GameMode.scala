package com.misiunas.mcs.game.config

import com.misiunas.mcs.R
import com.misiunas.mcs.game.tiles._


/**
  * Created by kmisiunas on 2016-06-23.
  */
abstract class GameMode extends Serializable {



  def isClassic: Boolean
  def hasObjectiveTile: Boolean
  def hasTreasures: Boolean
  def initialLives: Int

  def initialScore: Int = 0

  def timeScorePenalty(time: Long): Int = 0

  // todo reduce these numbers to use the same engine as mine placement
  def treasureDensities: Map[TreasureTile, Double] = Map(
    Pie -> 0.006,
    GoldOre ->  0.007,
    DiamondOre -> 0.002,
    Steak -> 0.001,
    Chest -> 0.0004
  )

}




object GameMode {

  case object Classic extends GameMode {
    override def toString: String = "classic"
    override  def initialLives: Int = 1
    override  def hasTreasures: Boolean = false
    override  def isClassic: Boolean = true
    override  def hasObjectiveTile: Boolean = false
    override def treasureDensities: Map[TreasureTile, Double] = Map()
  }

  case object Adventure extends GameMode {
    override def toString: String = "adventure"
    override  def initialLives: Int = 4
    override  def hasTreasures: Boolean = true
    override  def isClassic: Boolean = false
    override  def hasObjectiveTile: Boolean = false
  }

  case object Rescue extends GameMode {
    override def toString: String = "rescue"
    override def initialLives: Int = 4
    override def hasTreasures: Boolean = true
    override def isClassic: Boolean = false
    override def hasObjectiveTile: Boolean = true
    override def initialScore: Int = 1000
    override def timeScorePenalty(time: Long): Int = (time / 2000).toInt
  }

}

