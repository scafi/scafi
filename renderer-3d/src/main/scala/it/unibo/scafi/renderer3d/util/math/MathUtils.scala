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

package it.unibo.scafi.renderer3d.util.math

import scalafx.geometry.Point3D

/**
 * Contains math methods missing from java.lang.Math.
 * */
object MathUtils {

  /**
   * Rotates the given vector by the specified angle, around the specified axis.
   * From https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java
   *
   * @param vector the vector to rotate
   * @param axis   the axis aroundwhich the vector should be rotated
   * @param angle  the rotation angle to apply, in radians
   * @return the rotated vector
   **/
  def rotateVector(vector: Point3D, axis: Point3D, angle: Double): Point3D = {
    val (x, y, z) = (vector.x, vector.y, vector.z)
    val (u, v, w) = (axis.x, axis.y, axis.z)
    val angleSin = Math.sin(angle)
    val angleCos = Math.cos(angle)
    val refactoredPart = u * (u * x + v * y + w * z) * (1d - angleCos)
    val xPrime = u * refactoredPart + x * angleCos + (-w * y + v * z) * angleSin
    val yPrime = v * refactoredPart + y * angleCos + (w * x - u * z) * angleSin
    val zPrime = w * refactoredPart + z * angleCos + (-v * x + u * y) * angleSin
    new Point3D(xPrime, yPrime, zPrime)
  }
}
