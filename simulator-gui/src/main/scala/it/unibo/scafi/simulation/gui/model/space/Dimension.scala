package it.unibo.scafi.simulation.gui.model.space

sealed trait Dimension

class Dimension3D(val width : Double, val height : Double, val depth : Double) extends Dimension{
  def scale(scale : Double): Dimension3D = {
    new Dimension3D(width * scale, height * scale, depth * scale)
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Dimension3D]

  override def equals(other: Any): Boolean = other match {
    case that: Dimension3D =>
      (that canEqual this) &&
        width == that.width &&
        height == that.height &&
        depth == that.depth
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(width, height, depth)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Dimension3D {
  def apply(width: Double, height: Double, depth: Double) = new Dimension3D(width,height,depth)

  def unapply(d: Dimension3D): Option[(Double, Double, Double)] = Some(d.width,d.height,d.depth)
}


