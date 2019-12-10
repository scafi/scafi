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
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Shape3D

object RichScalaFx extends RichScalaFxHelper {

  object RichMath {
    def clamp(value: Double, min: Double, max: Double): Double =
      if (value < min) min else if (value > max) max else value
  }

  implicit class RichNode(node: Node) {
    final def getPosition: Point3D = node.delegate.getPosition

    final def getScreenPosition: Point2D = node.delegate.getScreenPosition

    final def moveTo(position: Point3D): Unit = node.delegate.moveTo(position)

    final def rotateOnSelf(angle: Double, axis: Point3D): Unit = node.delegate.rotateOnSelf(angle, axis)

    final def lookAtOnXZPlane(point: Point3D): Unit = node.delegate.lookAtOnXZPlane(point)

    final def getLookAtAngleOnXZPlane(point: Point3D): Double = node.delegate.getLookAtAngleOnXZPlane(point)

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

  implicit class RichMouseEvent(event: MouseEvent) {
    final def getScreenPosition: Point2D = new Point2D(event.getScreenX, event.getScreenY)
  }

  implicit class RichProduct3Double(product: Product3[Double, Double, Double]) {
    final def toPoint3D: Point3D = new Point3D(product._1, product._2, product._3)
  }
}
