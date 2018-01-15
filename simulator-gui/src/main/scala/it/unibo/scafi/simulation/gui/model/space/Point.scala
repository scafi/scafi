package it.unibo.scafi.simulation.gui.model.space

/**
  * root trait of all type of position
  */
sealed trait Point
object Point {
  object ZERO extends Point3D(0,0,0);
}
/**
  * describe a 3D position
  * @param x coordinate
  * @param y coordinate
  * @param z coordinate
  */
class Point3D(val x : Double,val y : Double,val z : Double) extends Point {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Point3D]

  override def equals(other: Any): Boolean = other match {
    case that: Point3D =>
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

  override def toString = s"Point3D($x, $y, $z)"
}

object Point3D {
    def apply(x: Double, y: Double, z: Double): Point3D = new Point3D(x,y,z)

    def unapply(p: Point3D): Option[(Double, Double, Double)] = Some(p.x,p.y,p.y)

    implicit def toPosition2D(p: Point3D) : Point2D = new Point2D(p.x,p.y)
}

/**
  * describe a 2D position
  * @param x coordinate
  * @param y coordinate
  */
class Point2D(val x : Double, val y : Double) extends Point {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Point2D]

  override def equals(other: Any): Boolean = other match {
    case that: Point2D =>
      (that canEqual this) &&
        x == that.x &&
        y == that.y
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Point2D($x, $y)"
}

object Point2D {
  def apply(x: Double, y: Double): Point2D = new Point2D(x,y)

  def unapply(p: Point2D): Option[(Double, Double)] = Some(p.x,p.y)
}

