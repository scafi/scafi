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

import javafx.scene.Group
import org.scalafx.extras._
import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import scalafx.geometry.Point3D
import scalafx.scene.paint.Color
import scalafx.scene.{CacheHint, Camera}

final case class SimpleNetworkNode(position: Point3D, labelText: String, UID: String) extends Group with NetworkNode {

  private[this] val NODE_SIZE = 60
  private[this] val DEFAULT_COLOR = Color.color(0.2, 0.2, 0.2)
  private[this] val SELECTION_MATERIAL = createMaterial(Color.Red)
  private[this] val DEFAULT_MATERIAL = createMaterial(DEFAULT_COLOR)
  private[this] val LABEL_FONT_SIZE = 200
  private[this] val node = createBox(NODE_SIZE, DEFAULT_COLOR, position)
  private[this] val labelPosition = new Point3D(position.x, position.y - (NODE_SIZE + 190), position.z)
  private[this] val label = createLabel(labelText, LABEL_FONT_SIZE, labelPosition)

  this.setId(UID)
  optimizeForSpeed()
  this.getChildren.addAll(node, label)

  private def optimizeForSpeed(): Unit = {
    node.cache = true
    label.cache = true
    node.setCacheHint(CacheHint.Speed)
    label.setCacheHint(CacheHint.Speed)
  }

  override def updateText(text: String): Unit = onFX(label.setText(text))

  override def rotateTextToCamera(camera: Camera): Unit = label.lookAtOnXZPlane(camera.getPosition)

  override def setColor(color: java.awt.Color): Unit = node.setColor(color)

  override def getNodePosition: Point3D = node.getPosition

  override def select(): Unit = {
    node.setScale(2)
    node.setMaterial(SELECTION_MATERIAL)
  }

  override def deselect(): Unit = {
    node.setScale(1)
    if(node.getMaterial == SELECTION_MATERIAL.delegate){
      node.setMaterial(DEFAULT_MATERIAL)
    }
  }

  override def increaseFontSize(): Unit = addLabelSize(0.1)

  override def decreaseFontSize(): Unit = addLabelSize(-0.1)

  private def addLabelSize(sizeDifference: Double): Unit = {
    val MIN_SCALE = 0.1
    val MAX_SCALE = 5
    label.setScale(RichMath.clamp(label.getScaleX + sizeDifference, MIN_SCALE, MAX_SCALE))
  }
}

object SimpleNetworkNode {
  def apply(position: Point3D, labelText: String, UID: String): SimpleNetworkNode =
    new SimpleNetworkNode(position, labelText, UID)
}