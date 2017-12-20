package it.unibo.scafi.simulation.gui.model.space

/**
  * root trait of all type of position
  */
sealed trait Position {}

/**
  * describe a 3D position
  * @param x coordinate
  * @param y coordinate
  * @param z coordinate
  */
class Position3D(val x : Double,val y : Double,val z : Double) extends Position {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Position3D]

  override def equals(other: Any): Boolean = other match {
    case that: Position3D =>
      (that canEqual this) &&
        x == that.x &&
        y == that.y &&
        z == that.z
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y, z)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Position3D {
    def apply(x: Double, y: Double, z: Double): Position3D = new Position3D(x,y,z)

    def unapply(p: Position3D): Option[(Double, Double, Double)] = Some(p.x,p.y,p.y)

    implicit def toPosition2D(p: Position3D) { Position2D(p.x,p.y)}
}

/**
  * describe a 2D position
  * @param x coordinate
  * @param y coordinate
  */
class Position2D(val x : Double, val y : Double) extends Position {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Position2D]

  override def equals(other: Any): Boolean = other match {
    case that: Position2D =>
      (that canEqual this) &&
        x == that.x &&
        y == that.y
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Position2D {
  def apply(x: Double, y: Double): Position2D = new Position2D(x,y)

  def unapply(p: Position2D): Option[(Double, Double)] = Some(p.x,p.y)
}

