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

package it.unibo.scafi.renderer3d.util

import com.typesafe.scalalogging.Logger
import it.unibo.scafi.renderer3d.util.math.FastMath
import javafx.scene.Group
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.transform.Rotate

/** Object that contains some implicit classes to enrich the language (using "Pimp my Library") when using ScalaFx. */
private[util] trait RichScalaFxHelper {
  private[this] val X_AXIS_2D = new Point2D(1, 0)
  private[this] val logger = Logger("RichScalaFxHelper")

  implicit class RichJavaPoint3D(point: javafx.geometry.Point3D) {
    /** Converts the javafx.geometry.Point3D to scalafx.geometry.Point3D
     * @return the point as a scalafx.geometry.Point3D */
    final def toScalaPoint: Point3D = new Point3D(point)
  }

  implicit class RichPoint2D(point: Point2D) {
    /** Calculates the angle (0-180) between the point (seen as a 2D direction) and the other specified 2D direction.
     * @param otherPoint the other point, seen as a 2D direction vector
     * @return the angle between the two 2D directions */
    final def eulerAngleTo(otherPoint: Point2D): Double = {
      val determinant = point.x * otherPoint.y - point.y * otherPoint.x
      FastMath.atan2(determinant.toFloat, point.dotProduct(otherPoint).toFloat).toDegrees
    }
  }

  implicit class RichPoint3D(point: Point3D) {
    /** @return the negated 3D direction */
    final def negate: Point3D = point * -1

    /** @return the point converted to a Product3 of Double */
    final def toProduct: Product3[Double, Double, Double] = (point.x, point.y, point.z)

    /** @param value the scalar value to multiply
     * @return the 3d direction vector multiplied by the provided scalar value */
    final def *(value: Double): Point3D = point.multiply(value).toScalaPoint

    /** @param value the scalar value to calculate the division
     * @return the 3d direction vector divided by the provided scalar value */
    final def /(value: Double): Point3D = new Point3D(point.x / value, point.y / value, point.z / value)

    /** @param otherPoint the 3d direction to be added
     * @return the 3d direction vector that is the sum of the two 3d direction vectors */
    final def +(otherPoint: Point3D): Point3D = point.add(otherPoint).toScalaPoint

    /** @param otherPoint the 3d direction to be subtracted
     * @return the 3d direction vector that is the result of subtraction between the two 3d direction vectors */
    final def -(otherPoint: Point3D): Point3D = point.subtract(otherPoint).toScalaPoint
  }

  implicit class RichJavaNode(node: javafx.scene.Node) {
    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#lookAt(scalafx.geometry.Point3D, scalafx.geometry.Point3D)]] */
    final def lookAt(point: Point3D, currentPosition: Point3D): Unit = onFX {
      node.lookAtOnXZPlane(point)
      val direction = point - currentPosition
      val angleOnX = new Point2D(direction.x, direction.y).eulerAngleTo(new Point2D(direction.x, 0)) + 90
      val finalAngleOnX = if(direction.x > 0) - (angleOnX + 180) else angleOnX
      val rotateOnX = new Rotate(finalAngleOnX, Rotate.XAxis)
      node.getTransforms.setAll(rotateOnX)
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#lookAtOnXZPlane(scalafx.geometry.Point3D)]] */
    final def lookAtOnXZPlane(point: Point3D): Unit =
      onFX {node.setRotationAxis(Rotate.YAxis); node.setRotate(getLookAtAngleOnXZPlane(point))}

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#getLookAtAngleOnXZPlane(scalafx.geometry.Point3D)]] */
    final def getLookAtAngleOnXZPlane(point: Point3D): Double = {
      val nodePosition = node.getPosition
      val directionOnXZPlane = new Point2D(point.x - nodePosition.x, point.z - nodePosition.z)
      directionOnXZPlane.eulerAngleTo(X_AXIS_2D) - 90
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#getPosition()]] */
    final def getPosition: Point3D = {
      printIllegalCallIfNeeded("getPosition")
      val transform = onFXAndWait(node.getLocalToSceneTransform)
      new Point3D(transform.getTx, transform.getTy, transform.getTz)
    }

    private[this] def printIllegalCallIfNeeded(methodName: String): Unit = node match {
        case _: Group => logger.error("Illegal call of method " + methodName + " on Node of type Group"); case _ => ()}

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#getScreenPosition()]] */
    final def getScreenPosition: Point2D = {
      val screenBounds = onFXAndWait(node.localToScreen(node.getBoundsInLocal))
      new Point2D((screenBounds.getMinX + screenBounds.getMaxX)/2, (screenBounds.getMinY + screenBounds.getMaxY)/2)
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#moveTo(scalafx.geometry.Point3D)]] */
    final def moveTo(position: Point3D): Unit = onFX {
      printIllegalCallIfNeeded("moveTo")
      node.setTranslateX(position.x)
      node.setTranslateY(position.y)
      node.setTranslateZ(position.z)
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#rotateOnSelf(double, scalafx.geometry.Point3D)]] */
    final def rotateOnSelf(angle: Double, axis: Point3D): Unit = onFX {
      node.setRotationAxis(axis)
      node.setRotate(node.getRotate + angle)
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#setScale(double)]] */
    final def setScale(scale: Double): Unit = onFX {
      node.setScaleX(scale)
      node.setScaleY(scale)
      node.setScaleZ(scale)
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#getYRotationAngle()]] */
    final def getYRotationAngle: Double = {
      val zx = node.getLocalToSceneTransform.getMzx
      val zz = node.getLocalToSceneTransform.getMzz
      FastMath.atan2(-zz.toFloat, zx.toFloat).toDegrees
    }

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichNode#isIntersectingWith(scalafx.scene.Node)]] */
    final def isIntersectingWith(otherNode: javafx.scene.Node): Boolean =
      otherNode!=null && node.getBoundsInParent.intersects(otherNode.getBoundsInParent)
  }
}
