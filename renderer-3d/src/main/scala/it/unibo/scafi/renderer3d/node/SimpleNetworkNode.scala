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
import javafx.scene.{Camera, Group, Node}
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.paint.Color
import scalafx.scene.shape.Sphere

/** An implementation of [[NetworkNode]], using a cube to represent the node. An instance is a node of the 3d network.*/
final case class SimpleNetworkNode(position: Point3D, UID: String, nodeColor: Color, labelScale: Double)
  extends Group with NetworkNode {

  private[this] val NODE_SIZE = 6
  private[this] val LABEL_FONT_SIZE = 30 //ATTENTION: big labels cause performance issues, but small fonts are blurry
  private[this] val LABEL_ADDED_HEIGHT = NODE_SIZE
  private[this] val node = createCube(NODE_SIZE, nodeColor, position)
  private[this] val label = createText("", LABEL_FONT_SIZE, getLabelPosition(position))
  private[this] val seeThroughSphere = createOutlinedSphere(1, position)
  private[this] val filledSphere = createFilledSphere(1, position)
  private[this] var state = NetworkNodeState(nodeColor, position)

  setLabelScale(labelScale)
  label.setVisible(false)
  this.getChildren.addAll(node) //label is not added by default for performance reasons, since it would show "" anyway

  private def getLabelPosition(nodePosition: Point3D, addedHeight: Double = LABEL_ADDED_HEIGHT): Point3D =
    new Point3D(nodePosition.x, nodePosition.y - (NODE_SIZE + addedHeight), nodePosition.z)

  /** See [[NetworkNode.setText]] */
  override def setText(text: String, camera: Camera): Unit = onFX{
    if(text == "") {
      if(label.isVisible) {label.setVisible(false); this.getChildren.remove(label)}
    } else if(!label.isVisible) {
      reAddLabel(text, camera)
    } else {
      label.setText(text)
    }
  }

  private def reAddLabel(text: String, camera: Camera): Unit = {
    label.setText(text)
    label.setVisible(true)
    this.getChildren.add(label)
    rotateTextToCamera(camera.getPosition)
  }

  /** See [[NetworkNode.rotateTextToCamera]] */
  override def rotateTextToCamera(cameraPosition: Point3D): Unit =
    onFX {if(label.isVisible) label.lookAtOnXZPlane(cameraPosition)}

  /** See [[NetworkNode.setFilledSphereColor]] */
  override def setFilledSphereColor(color: Color): Unit = onFX {filledSphere.setColor(new Color(color.opacity(0.1)))}

  /** See [[NetworkNode.setNodeColor]] */
  override def setNodeColor(color: Color): Unit = onFX {node.setColor(color); state = state.copy(currentColor = color)}

  /** See [[NetworkNode.setSelectionColor]] */
  override def setSelectionColor(color: Color): Unit =
    onFX {if(isSelected) node.setColor(color); state = state.copy(selectionColor = color)}

  private def isSelected: Boolean = node.getScaleX > this.state.scale

  /** See [[NetworkNode.getNodePosition]] */
  override def getNodePosition: Point3D = state.currentPosition //not using node.getPosition to improve performance

  /** See [[NetworkNode.select]] */
  override def select(): Unit = {node.setScale(2*this.state.scale); node.setColor(state.selectionColor)}

  /** See [[NetworkNode.deselect]] */
  override def deselect(): Unit =
    onFX {if(isSelected) {node.setScale(this.state.scale); node.setColor(state.currentColor)}}

  /** See [[NetworkNode.setLabelScale]] */
  override def setLabelScale(scale: Double): Unit = label.setScale(scale * NODE_SIZE/18)

  /** See [[NetworkNode.setSeeThroughSphereRadius]] */
  override def setSeeThroughSphereRadius(radius: Double): Unit = setSphereRadius(seeThroughSphere, radius)

  /** See [[NetworkNode.setFilledSphereRadius]] */
  override def setFilledSphereRadius(radius: Double): Unit = setSphereRadius(filledSphere, radius)

  private def setSphereRadius(sphere: Sphere, radius: Double): Unit = onFX {
    if(radius < 1){
      this.getChildren.remove(sphere)
    } else if(!this.getChildren.contains(sphere)) {
      this.getChildren.add(sphere)
      sphere.setRadius(radius)
    }
  }

  /** See [[NetworkNode.moveNodeTo]] */
  override def moveNodeTo(position: Point3D): Unit = onFX {
    List(node, seeThroughSphere, filledSphere).foreach(_.moveTo(position))
    label.moveTo(getLabelPosition(position))
    state = state.copy(currentPosition = position)
  }

  /** See [[NetworkNode.setNodeScale]] */
  override def setNodeScale(newScale: Double): Unit = onFX {
    state = state.copy(scale = newScale)
    List(node, filledSphere).foreach(_.setScale(newScale))
    label.moveTo(getLabelPosition(node.getPosition, LABEL_ADDED_HEIGHT*(1 + newScale/5)))
  }

  /** See [[NetworkNode.nodeIntersectsWith]] */
  override def nodeIntersectsWith(node: Node): Boolean = this.node.delegate.isIntersectingWith(node)
}

object SimpleNetworkNode {
  def apply(position: Point3D, UID: String, nodeColor: Color, labelScale: Double): SimpleNetworkNode =
    new SimpleNetworkNode(position, UID, nodeColor, labelScale)
}