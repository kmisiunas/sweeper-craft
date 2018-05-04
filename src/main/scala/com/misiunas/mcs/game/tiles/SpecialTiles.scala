package com.misiunas.mcs.game.tiles

import com.misiunas.mcs.game.Tool.Mine

/**
  * Created by kmisiunas on 2016-07-02.
  */

abstract class SpecialTile extends Tile

object Sword extends SpecialTile {
  override def getImage: Int = 27
  override def openAction(action: Action): Unit = () // should never be opened
  override def clickAction(action: Action): Unit = {
    action.operator.addAction("found_sword")
    action.operator.cleanPos( action.pos ) // must be on empty tiles
    action.operator.addSword()
  }
}


object Explosives extends SpecialTile {
  override def getImage: Int = 21
  override def openAction(action: Action): Unit = () // should never be opened
  override def clickAction(action: Action): Unit = {
    val operator = action.operator
    operator.set(action.pos -> EmptyTile)
    val clearThese = action.pos.nineBlock
    action.pos.nineBlock  // boom - clean
      .filter( operator.isMonster(_) )
      .foreach( operator.cleanPos(_) ) // auto normalises the count
    action.pos.nineBlock // and open
      .filter( operator.get(_).isHidden )
      .foreach( pos => operator.get(pos).clickAction(ActionAutomatic(Mine, pos, operator)))
    operator.addAction("explosives_boom")
  }
}

object Goal extends SpecialTile {
  override def getImage: Int = 28
  override def openAction(action: Action): Unit = {
    action.operator.addAction("found_goal")
    action.operator.addAction("won")
  }
  override def clickAction(action: Action): Unit = {
    action.operator.addAction("found_goal")
    action.operator.addAction("won")
  }
}

object StartingPoint extends SpecialTile {
  override def getImage: Int = 29
  override def openAction(action: Action): Unit = ()
  override def clickAction(action: Action): Unit = {
    action.operator.addAction("show_tutorial")
  }
}


object ErrorTile extends SpecialTile {
  override def getImage: Int = 47
  override def openAction(action: Action): Unit = ()
  override def clickAction(action: Action): Unit = action.operator.addAction("click_start")
}