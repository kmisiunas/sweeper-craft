package com.misiunas.mcs.game

/**
  * Replacing old coordinate class
  *
  * Created by kmisiunas on 2016-06-16.
  */
class Pos private (val x: Int, val y: Int) extends Serializable {

  def copy(x:Int = this.x, y:Int = this.y): Pos = Pos(x, y)

  def + (p2: Pos): Pos = Pos(x+p2.x, y+p2.y)
  def - (p2: Pos): Pos = Pos(x-p2.x, y-p2.y)
  def * (i: Int): Pos = Pos(x*i, y*i)
  def / (i: Int): Pos = Pos(x/i, y/i)

  def distance(p2: Pos): Double = Math.sqrt( (p2.x - x)*(p2.x - x) + (p2.y - y)*(p2.y - y) )

  def around: List[Pos] = List(
    Pos(x-1,y-1), Pos(x, y-1), Pos(x+1,y-1),
    Pos(x+1, y),
    Pos(x+1, y+1), Pos(x, y+1), Pos(x-1, y+1),
    Pos(x-1, y)
  )

  def nineBlock: List[Pos] = around :+ this

  def within(limit: Pos): Boolean = x >= 0 && y >= 0 && x < limit.x && y < limit.y

  override def equals(that: Any): Boolean = that match {
    case that: Pos => that.x == this.x && that.y == this.y
    case _ => false
  }

  def toPosFloat: PosFloat = PosFloat(x,y)

  override def toString: String = "Pos("+x+", "+y+")"

}

object Pos {

  def apply(x: Int,  y: Int): Pos = new Pos( x, y )

  def random(max: Pos): Pos = Pos( (math.random * max.x).toInt, (math.random * max.y).toInt )

  def range(max: Pos): Vector[Pos] = (for (i <- 0 until max.x; j <- 0 until max.y) yield Pos(i,j)).toVector

}


/** Special variation for real vectors */
case class PosFloat (val x: Float, val y: Float) extends Serializable {
  def + (p2: Pos): PosFloat = PosFloat(x+p2.x, y+p2.y)
  def - (p2: Pos): PosFloat = PosFloat(x-p2.x, y-p2.y)
  def * (f: Float): PosFloat = PosFloat(x*f, y*f)
  def * (f: Int): PosFloat = PosFloat(x*f, y*f)
  def / (f: Int): PosFloat = PosFloat(x/f, y/f)
  def toPos: Pos = Pos(x.toInt, y.toInt)
}