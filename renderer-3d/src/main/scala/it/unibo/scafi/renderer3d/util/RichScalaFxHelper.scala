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

import it.unibo.scafi.renderer3d.node.NetworkNode
import org.scalafx.extras.{onFX, onFXAndWait}
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.transform.Rotate

private[util] trait RichScalaFxHelper {

  implicit class RichJavaPoint3D(point: javafx.geometry.Point3D) {
    final def toScalaPoint: Point3D = new Point3D(point)
  }

  implicit class RichPoint2D(point: Point2D) {
    final def eulerAngleTo(otherPoint: Point2D): Double = {
      val determinant = point.x * otherPoint.y - point.y * otherPoint.x
      Math.atan2(determinant, point.dotProduct(otherPoint)).toDegrees
    }
  }

  implicit class RichPoint3D(point: Point3D) {
    final def negate: Point3D = point * -1
    final def toProduct: Product3[Double, Double, Double] = (point.x, point.y, point.z)

    final def *(value: Double): Point3D = point.multiply(value).toScalaPoint
    final def /(value: Double): Point3D = new Point3D(point.x / value, point.y / value, point.z / value)
    final def +(otherPoint: Point3D): Point3D = point.add(otherPoint).toScalaPoint
    final def -(otherPoint: Point3D): Point3D = point.subtract(otherPoint).toScalaPoint
  }

  implicit class RichJavaNode(node: javafx.scene.Node) {
    final def lookAtOnXZPlane(point: Point3D): Unit = {
      val yAngle = getLookAtAngleOnXZPlane(point)
      onFX {node.setRotationAxis(Rotate.YAxis); node.setRotate(yAngle)}
    }

    final def getLookAtAngleOnXZPlane(point: Point3D): Double = {
      val directionToPoint = (point - node.getPosition).normalize()
      val directionOnXZPlane = new Point2D(directionToPoint.getX, directionToPoint.getZ)
      directionOnXZPlane.eulerAngleTo(new Point2D(1, 0)) - 90
    }

    /**
     * Don't use this on Group objects such as SimpleNetworkNode, since it would always return Point3D(0, 0, 0)
     * */
    final def getPosition: Point3D = {
      val transform = onFXAndWait(node.getLocalToSceneTransform)
      new Point3D(transform.getTx, transform.getTy, transform.getTz)
    }

    final def getScreenPosition: Point2D = {
      val screenBounds = onFXAndWait(node.localToScreen(node.getBoundsInLocal))
      new Point2D(screenBounds.getMinX, screenBounds.getMinY)
    }

    /**
     * Using this on Group objects such as SimpleNetworkNode adds the position to the current position of the object,
     * instead of actually moving the node at the specified position.
     * */
    final def moveTo(position: Point3D): Unit = onFX {
      node.setTranslateX(position.x)
      node.setTranslateY(position.y)
      node.setTranslateZ(position.z)
    }

    final def rotateOnSelf(angle: Double, axis: Point3D): Unit =
      onFX {node.getTransforms.add(new Rotate(angle, 0, 0, 0, axis))}

    final def toNetworkNode: NetworkNode = node match {case networkNode: NetworkNode => networkNode}

    final def setScale(scale: Double): Unit = onFX {
      node.setScaleX(scale)
      node.setScaleY(scale)
      node.setScaleZ(scale)
    }
  }

}
