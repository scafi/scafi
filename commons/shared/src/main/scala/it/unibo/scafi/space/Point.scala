/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.space

import scala.language.implicitConversions

class Point3D(val x: Double, val y: Double, val z: Double) extends Serializable with Product3[Double, Double, Double] {
  import scala.math.{sqrt, pow}

  def distance(p2: Point3D): Double = sqrt(pow(p2.x-x, 2) + pow(p2.y-y, 2) + pow(p2.z-z, 2))

  def +(p2: Point3D): Point3D = new Point3D(x + p2.x, y + p2.y, z + p2.z)

  override def toString(): String = s"($x;$y;$z)"

  override def equals(other: Any): Boolean = other match {
    case Point3D(x, y, z) => this.x == x && this.y == y && this.z == z
    case _ => false
  }

  override def hashCode(): Int = Seq(x, y, z).hashCode()

  override def _1: Double = x

  override def _2: Double = y

  override def _3: Double = z

  override def canEqual(otherObject: Any): Boolean = otherObject match {case _: Point3D => true; case _ => false}
}
object Point3D {
  def unapply(d: Point3D): Option[(Double, Double, Double)] = Some(d.x, d.y, d.z)

  def apply(x: Double, y: Double, z: Double): Point3D = new Point3D(x,y,z)

  val Zero: Point3D = Point3D(0,0,0)

  implicit def toPoint2D(p: Point3D): Point2D = Point2D(p.x, p.y)
  implicit def toPoint1D(p: Point3D): Point1D = Point1D(p.x)
}

class Point2D(x: Double, y: Double) extends Point3D(x, y, 0) {
  override def toString(): String = s"($x;$y)"
}
object Point2D {
  def unapply(d: Point2D): Option[(Double, Double)] = Some(d.x, d.y)

  def apply(x: Double, y: Double): Point2D = new Point2D(x,y)

  implicit def toPoint3D(p: Point2D): Point3D = p.asInstanceOf[Point3D]
  implicit def toPoint1D(p: Point2D): Point1D = Point1D(p.x)
}

class Point1D(x: Double) extends Point2D(x, 0) {
  override def toString(): String = s"($x)"
}
object Point1D {
  def apply(x: Double): Point1D = new Point1D(x)

  implicit def toPoint3D(p: Point1D): Point3D = p.asInstanceOf[Point3D]
  implicit def toPoint2D(p: Point1D): Point2D = p.asInstanceOf[Point2D]
}
