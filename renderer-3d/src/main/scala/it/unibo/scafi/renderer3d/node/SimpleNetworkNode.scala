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
import scalafx.scene.paint.Color
import scalafx.scene.{CacheHint, Camera}

final case class SimpleNetworkNode(position: Point3D, UID: String, nodeColor: Color, labelScale: Double)
  extends Group with NetworkNode {

  private[this] val NODE_SIZE = 60
  private[this] val LABEL_FONT_SIZE = 200
  private[this] val node = createBox(NODE_SIZE, nodeColor, position)
  private[this] val labelPosition = getLabelPosition(position)
  private[this] val label = createLabel("", LABEL_FONT_SIZE, labelPosition)
  private[this] var currentColor = nodeColor
  private[this] var selectionColor = Color.Red

  setLabelScale(labelScale)
  this.setId(UID)
  optimizeForSpeed()
  this.getChildren.addAll(node, label)

  private def getLabelPosition(nodePosition: Point3D): Point3D =
    new Point3D(nodePosition.x, nodePosition.y - (NODE_SIZE + 190), nodePosition.z)

  private def optimizeForSpeed(): Unit = {
    node.cache = true
    label.cache = true
    node.setCacheHint(CacheHint.Speed)
    label.setCacheHint(CacheHint.Speed)
  }

  override def updateText(text: String): Unit = onFX(label.setText(text))

  override def rotateTextToCamera(camera: Camera): Unit = label.lookAtOnXZPlane(camera.getPosition)

  override def setNodeColor(color: Color): Unit = onFX {
    node.setColor(color)
    currentColor = color
  }

  override def setSelectionColor(color: Color): Unit = onFX {
    if(isSelected){
      node.setColor(color)
    }
    selectionColor = color
  }

  private def isSelected: Boolean = node.getScaleX > 1

  override def getNodePosition: Point3D = node.getPosition

  override def select(): Unit = onFX {
    node.setScale(2)
    node.setColor(selectionColor)
  }

  override def deselect(): Unit = onFX {
    if(isSelected){
      node.setScale(1)
      node.setColor(currentColor)
    }
  }

  override def setLabelScale(scale: Double): Unit = onFX {label.setScale(scale)}

  override def moveNodeTo(position: Point3D): Unit = onFX {
    node.moveTo(position)
    label.moveTo(getLabelPosition(position))
  }
}

object SimpleNetworkNode {
  def apply(position: Point3D, UID: String, nodeColor: Color, labelScale: Double): SimpleNetworkNode =
    new SimpleNetworkNode(position, UID, nodeColor, labelScale)
}