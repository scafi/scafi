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
import it.unibo.scafi.renderer3d.util.Rendering3DUtils.createMaterial
import javafx.scene.input.MouseEvent
import org.scalafx.extras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.{Node, Scene}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Shape3D
import scalafx.scene.transform.Rotate

object RichScalaFx {
  implicit class RichPoint3D(point: Point3D) {
    final def absolute: Point3D = new Point3D(point.x.abs, point.y.abs, point.z.abs)
    final def negate: Point3D = point * -1

    final def *(value: Double): Point3D = point.multiply(value).toScalaPoint
    final def /(value: Double): Point3D = new Point3D(point.x / value, point.y / value, point.z / value)
    final def +(otherPoint: Point3D): Point3D = point.add(otherPoint).toScalaPoint
    final def -(otherPoint: Point3D): Point3D = point.subtract(otherPoint).toScalaPoint
  }

  implicit class RichJavaPoint3D(point: javafx.geometry.Point3D) {
    final def toScalaPoint: Point3D = new Point3D(point)
  }

  implicit class RichPoint2D(point: Point2D) {
    final def eulerAngleTo(otherPoint: Point2D): Double = {
      val determinant = point.x * otherPoint.y - point.y * otherPoint.x
      Math.atan2(determinant, point.dotProduct(otherPoint)).toDegrees
    }
  }

  object RichMath {
    def clamp(value: Double, min: Double, max: Double): Double =
      if (value < min) min else if (value > max) max else value
  }

  implicit class RichJavaNode(node: javafx.scene.Node) { //TODO: ottimizza, ruota solo se necessario
    final def lookAtOnXZPlane(point: Point3D): Unit = {
      val directionToPoint = (point - node.getPosition).normalize()
      val directionOnXZPlane = new Point2D(directionToPoint.getX, directionToPoint.getZ)
      val xAngle = directionOnXZPlane.eulerAngleTo(new Point2D(1, 0))
      onFX {node.setRotationAxis(Rotate.YAxis); node.setRotate(xAngle - 90)}
    }

    /**
     * Don't use this on Group objects, since it would always return Point3D(0, 0, 0)
     * */
    final def getPosition: Point3D = {
      val transform = onFXAndWait(node.getLocalToSceneTransform)
      new Point3D(transform.getTx, transform.getTy, transform.getTz)
    }

    final def getScreenPosition: Point2D = {
      val screenBounds = node.localToScreen(node.getBoundsInLocal)
      new Point2D(screenBounds.getMinX, screenBounds.getMinY)
    }

    final def moveTo(position: Point3D): Unit = onFX {
      node.setTranslateX(position.x)
      node.setTranslateY(position.y)
      node.setTranslateZ(position.z)
    }

    final def rotateOnSelf(angle: Double, axis: Point3D): Unit =
      onFX {node.getTransforms.add(new Rotate(angle, 0, 0, 0, axis))}

    final def toNetworkNode: NetworkNode = node match {case networkNode: NetworkNode => networkNode}

    final def setScale(scale: Double): Unit = {
      node.setScaleX(scale)
      node.setScaleY(scale)
      node.setScaleZ(scale)
    }
  }

  implicit class RichNode(node: Node) {
    final def getPosition: Point3D = node.delegate.getPosition
    final def getScreenPosition: Point2D = node.delegate.getScreenPosition
    final def moveTo(position: Point3D): Unit = node.delegate.moveTo(position)
    final def rotateOnSelf(angle: Double, axis: Point3D): Unit = node.delegate.rotateOnSelf(angle, axis)
    final def lookAtOnXZPlane(point: Point3D): Unit = node.delegate.lookAtOnXZPlane(point)
    final def toNetworkNode: NetworkNode = node.delegate.toNetworkNode
    final def setScale(scale: Double): Unit = node.delegate.setScale(scale)
  }

  implicit class RichShape3D(shape: Shape3D) {
    final def setColor(color: java.awt.Color): Unit = {
        val material = createMaterial(color.toScalaFx)
        onFX(shape.setMaterial(material))
      }

    final def setColor(color: Color): Unit = onFX(shape.setMaterial(createMaterial(color)))
  }

  implicit class RichColor(color: java.awt.Color) {
    final def toScalaFx: Color = Color.rgb(color.getRed, color.getGreen, color.getBlue, color.getAlpha/255)
  }

  implicit class RichScene(scene: Scene) {
    final def findNodeById(id: String): Option[Node] = scene.lookup(s"#$id")
  }

  implicit class RichMouseEvent(event: MouseEvent) {
    final def getScreenPosition: Point2D = new Point2D(event.getScreenX, event.getScreenY)
  }
}
