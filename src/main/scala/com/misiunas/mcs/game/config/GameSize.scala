package com.misiunas.mcs.game.config

import com.misiunas.mcs.game.Pos

/**
  * Created by kmisiunas on 2016-06-23.
  */
case class GameSize (val size: Pos) extends Serializable


object GameSize {

  def small: GameSize =  GameSize( Pos(8,8) )

  def medium: GameSize =  GameSize(Pos(20,20))

  def large: GameSize =  GameSize (Pos(40,40))

  def infinite: GameSize =  GameSize (Pos(200,200))

}
