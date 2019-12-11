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

package it.unibo.scafi.renderer3d.node

import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Group
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.CacheHint
import scalafx.scene.paint.Color
import scalafx.scene.shape.Sphere

final case class SimpleNetworkNode(position: Point3D, UID: String, nodeColor: Color, labelScale: Double)
  extends Group with NetworkNode {

  private[this] val NODE_SIZE = 60
  private[this] val LABEL_FONT_SIZE = 200
  private[this] val LABEL_ADDED_HEIGHT = 180
  private[this] val node = createBox(NODE_SIZE, nodeColor, position)
  private[this] val labelPosition = getLabelPosition(position)
  private[this] val label = createLabel("", LABEL_FONT_SIZE, labelPosition)
  private[this] var currentColor = nodeColor
  private[this] var selectionColor = Color.Red
  private[this] val seeThroughSphere = createSeeThroughSphere(1, position)
  private[this] val filledSphere = createFilledSphere(1, position)

  setLabelScale(labelScale)
  this.setId(UID)
  optimizeForSpeed()
  this.getChildren.addAll(node, label)

  private def getLabelPosition(nodePosition: Point3D, addedHeight: Double = LABEL_ADDED_HEIGHT): Point3D =
    new Point3D(nodePosition.x, nodePosition.y - (NODE_SIZE + addedHeight), nodePosition.z)

  private def optimizeForSpeed(): Unit =
    List(node, label, seeThroughSphere, filledSphere).foreach(element => {
      element.cache = true
      element.setCacheHint(CacheHint.Speed)
    })

  override def setText(text: String): Unit = onFX(label.setText(text))

  override def rotateTextToCamera(cameraPosition: Point3D): Unit = label.lookAtOnXZPlane(cameraPosition)

  override def setFilledSphereColor(color: Color): Unit = onFX {filledSphere.setColor(new Color(color.opacity(0.1)))}

  override def setNodeColor(color: Color): Unit = onFX {node.setColor(color); currentColor = color}

  override def setSelectionColor(color: Color): Unit = onFX {
    if(isSelected){
      node.setColor(color)
    }
    selectionColor = color
  }

  private def isSelected: Boolean = node.getScaleX > 1

  override def getNodePosition: Point3D = node.getPosition

  override def select(): Unit = {node.setScale(2); node.setColor(selectionColor)}

  override def deselect(): Unit = onFX {
    if(isSelected){
      node.setScale(1)
      node.setColor(currentColor)
    }
  }

  override def setLabelScale(scale: Double): Unit = label.setScale(scale)

  override def setSeeThroughSphereRadius(radius: Double): Unit = setSphereRadius(seeThroughSphere, radius)

  override def setFilledSphereRadius(radius: Double): Unit = setSphereRadius(filledSphere, radius)

  private def setSphereRadius(sphere: Sphere, radius: Double): Unit = onFX {
    if(radius < 1){
      this.getChildren.remove(sphere)
    } else if(!this.getChildren.contains(sphere)) {
      this.getChildren.add(sphere)
      sphere.setRadius(radius)
    }
  }

  override def moveNodeTo(position: Point3D): Unit = {
    List(node, seeThroughSphere, filledSphere).foreach(_.moveTo(position))
    label.moveTo(getLabelPosition(position))
  }

  override def setNodeScale(scale: Double): Unit = {
    List(node, filledSphere).foreach(_.setScale(scale))
    label.moveTo(getLabelPosition(node.getPosition, LABEL_ADDED_HEIGHT*(1 + scale/5)))
  }
}

object SimpleNetworkNode {
  def apply(position: Point3D, UID: String, nodeColor: Color, labelScale: Double): SimpleNetworkNode =
    new SimpleNetworkNode(position, UID, nodeColor, labelScale)
}