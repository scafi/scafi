/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.space

import scala.language.implicitConversions

class Point3D(val x: Double, val y: Double, val z: Double) extends Serializable {
  import scala.math.{sqrt, pow}

  def distance(p2: Point3D): Double = sqrt(pow(p2.x-x, 2) + pow(p2.y-y, 2) + pow(p2.z-z, 2))

  def +(p2: Point3D): Point3D = new Point3D(x + p2.x, y + p2.y, z + p2.z)

  override def toString(): String = s"($x;$y;$z)"

  override def equals(other: Any): Boolean = other match {
    case Point3D(x, y, z) => this.x == x && this.y == y && this.z == z
    case _ => false
  }

  override def hashCode(): Int = Seq(x, y, z).hashCode()
}
object Point3D {
  def unapply(d: Point2D): Option[(Double, Double, Double)] = Some(d.x, d.y, d.z)

  def apply(x: Double, y: Double, z: Double): Point3D = new Point3D(x,y,z)

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
