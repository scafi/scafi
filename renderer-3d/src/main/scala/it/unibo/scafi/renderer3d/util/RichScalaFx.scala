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

/** Object that contains implicit classes to enrich the language (using "Pimp my Library"), raising the level of
 * abstraction when working with ScalaFx and JavaFx. */
object RichScalaFx extends RichScalaFxHelper {

  implicit class RichNode(node: Node) {
    /** Gets the node's position in the 3D scene. ATTENTION: Don't use this on Group objects such as SimpleNetworkNode,
     *  since it would always return Point3D(0, 0, 0)
     * @return the node's position */
    final def getPosition: Point3D = node.delegate.getPosition

    /** Gets the node's 2D position, from the point of view of the camera
     * @return the node's 2D position */
    final def getScreenPosition: Point2D = node.delegate.getScreenPosition

    /** Moves the node to another 3D position. ATTENTION: Using this on Group objects such as SimpleNetworkNode adds the
     *  position to the current position of the object, instead of actually moving the node at the specified position.
     * @param position the new position of the node
     * @return Unit, since it has the side effect of moving the node */
    final def moveTo(position: Point3D): Unit = node.delegate.moveTo(position)

    /** Rotates the node around itself, with the specified rotation axis
     * @param angle the rotation angle
     * @param axis the rotation axis
     * @return Unit, since it has the side effect of rotating the node */
    final def rotateOnSelf(angle: Double, axis: Point3D): Unit = node.delegate.rotateOnSelf(angle, axis)

    /** Rotates the node around itself but only on the Y axis, so that it faces the specified point.
     * @param point the point that the node will face
     * @return Unit, since it has the side effect of rotating the node */
    final def lookAtOnXZPlane(point: Point3D): Unit = node.delegate.lookAtOnXZPlane(point)

    /** Retrieves the angle between the X axis and the vector from the node's position and the specified 3d position.
     * @param point the specified 3d position used to calculate the angle
     * @return the rotation angle on the Y axis */
    final def getLookAtAngleOnXZPlane(point: Point3D): Double = node.delegate.getLookAtAngleOnXZPlane(point)

    /** Converts the node to a NetworkNode.
     * @return the casted node */
    final def toNetworkNode: NetworkNode = node.delegate.toNetworkNode

    /** Sets the scale of the node.
     * @param scale the new scale of the node
     * @return Unit, since it has the side effect of scaling the node */
    final def setScale(scale: Double): Unit = node.delegate.setScale(scale)

    /** Retrieves the euler angle on the Y axis of the current node.
     * @return the angle on the Y axis, in the range from -180 to 180. */
    final def getYRotationAngle: Double = node.delegate.getYRotationAngle
  }

  implicit class RichShape3D(shape: Shape3D) {
    /** See [[RichJavaShape3D.setColor]] */
    final def setColor(color: java.awt.Color): Unit = shape.delegate.setColor(color)

    /** See [[RichJavaShape3D.setColor]] */
    final def setColor(color: Color): Unit = shape.delegate.setColor(color)
  }

  implicit class RichJavaShape3D(shape: javafx.scene.shape.Shape3D) {
    /** Sets the color of the shape using java.awt.Color
     * @param color the new java.awt.Color of the shape
     * @return Unit, since it has the side effect of changing the color of the shape */
    final def setColor(color: java.awt.Color): Unit =
    {val material = createMaterial(color.toScalaFx); onFX(shape.setMaterial(material))}

    /** Sets the color of the shape using scalafx.scene.paint.Color.
     * @param color the new scalafx.scene.paint.Color of the shape
     * @return Unit, since it has the side effect of changing the color of the shape */
    final def setColor(color: Color): Unit = onFX(shape.setMaterial(createMaterial(color)))
  }

  implicit class RichColor(color: java.awt.Color) {
    /** Converts the java.awt.Color to scalafx.scene.paint.Color
     * @return the color of type scalafx.scene.paint.Color */
    final def toScalaFx: Color = Color.rgb(color.getRed, color.getGreen, color.getBlue, color.getAlpha/255)
  }

  implicit class RichMouseEvent(event: MouseEvent) {
    /** Gets the current 2D screen position of the mouse.
     * @return the screen position of the mouse */
    final def getScreenPosition: Point2D = new Point2D(event.getScreenX, event.getScreenY)
  }

  implicit class RichProduct3Double(product: Product3[Double, Double, Double]) {
    /** Converts the Product3 of Double to scalafx.geometry.Point3D
     * @return the product as scalafx.geometry.Point3D */
    final def toPoint3D: Point3D = new Point3D(product._1, product._2, product._3)
  }
}
