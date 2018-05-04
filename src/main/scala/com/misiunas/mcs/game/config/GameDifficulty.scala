package com.misiunas.mcs.game.config


/**
  * Created by kmisiunas on 2016-06-23.
  */
abstract class GameDifficulty extends Serializable {

  def mineDensity(gm: GameMode): Double

}

object GameDifficulty {

  def easy: GameDifficulty = Easy
  def medium: GameDifficulty = Medium
  def hard: GameDifficulty = Hard

  case object Easy extends GameDifficulty {
    override def mineDensity(gm: GameMode): Double =
      0.22 + (if (gm == GameMode.Classic) -0.02 else 0.0)
  }

  case object Medium extends GameDifficulty {
    override def mineDensity(gm: GameMode): Double =
      0.25 + (if (gm == GameMode.Classic) -0.02 else 0.0)
  }

  case object Hard extends GameDifficulty {
    override def mineDensity(gm: GameMode): Double =
      0.29 + (if (gm == GameMode.Classic) -0.02 else 0.0)
  }

}
