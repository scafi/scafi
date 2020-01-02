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

import javafx.scene.{Camera, Node}
import scalafx.geometry.Point3D
import scalafx.scene.paint.Color

/**
 * Interface of a single node of the 3d network. With this interface it's possible to move the node, set the text, the
 * colors, select or deselect, etc.
 * */
trait NetworkNode extends Node{

  /** Sets the text of the node's label.
   * @param text the new text to set
   * @param camera the scene's camera. This is needed if the label is currently deactivated, so it can face the camera
   * @return Unit, since it has the side effect of setting the text of the node's label */
  def setText(text: String, camera: Camera): Unit

  /** Rotates the label of the node so that the label faces the camera
   * @param cameraPosition the camera position
   * @return Unit, since it has the side effect of rotating the node's label */
  def rotateTextToCamera(cameraPosition: Point3D): Unit

  /** Sets the radius of the outlined sphere that surrounds the node
   * @param radius the new radius of the sphere. If the value is 0 the sphere should be deactivated.
   * @return Unit, since it has the side effect of setting the sphere's radius */
  def setSeeThroughSphereRadius(radius: Double): Unit

  /** Sets the radius of the colored sphere that surrounds the node
   * @param radius the new radius of the sphere. If the value is 0 the sphere should be deactivated.
   * @return Unit, since it has the side effect of setting the sphere's radius */
  def setFilledSphereRadius(radius: Double): Unit

  /** Sets the color of the colored sphere that surrounds the node
   * @param color the new color of the sphere.
   * @return Unit, since it has the side effect of setting the sphere's color */
  def setFilledSphereColor(color: Color): Unit

  /** Sets the color of the shape that represents the current node
   * @param color the new color of the cube.
   * @return Unit, since it has the side effect of setting the cube's color */
  def setNodeColor(color: Color): Unit

  /** Sets the selection color of the cube that represents the current node, so that whenever the cube is selected it
   *  has the specified color.
   * @param color the new selection color of the cube.
   * @return Unit, since it has the side effect of setting the cube's selection color */
  def setSelectionColor(color: Color): Unit

  /** Selects the node, making it bigger and changing its color to the selection color.
   * @return Unit, since it has the side effect of selecting the node */
  def select(): Unit

  /** Deselects the node, resizing it to default and changing its color to the default color.
   * @return Unit, since it has the side effect of deselecting the node */
  def deselect(): Unit

  /** Sets the scale of the node's label. ATTENTION: big labels cause performance issues.
   * @param scale the new scale of the label
   * @return Unit, since it has the side effect of setting the scale of the node's label */
  def setLabelScale(scale: Double): Unit

  /** Retrieves the current position in the 3d scene of the node.
   * @return the current position of the node */
  def getNodePosition: Point3D

  /** Moves the node to the specified position in the 3d scene. IMPORTANT: don't use other methods to move this node
   * @param position the new position of the node
   * @return Unit, since it has the side effect of moving the node */
  def moveNodeTo(position: Point3D): Unit

  /** Sets the scale of the shape representing the node.
   * @param scale the new scale of the node
   * @return Unit, since it has the side effect of setting the scale of the node */
  def setNodeScale(scale: Double): Unit

  /** Checks if the cube of the node is intersecting with the provided node's mesh. ATTENTION: this is inaccurate.
   * @param node the node to check for intersection
   * @return whether the two nodes are intersecting */
  def nodeIntersectsWith(node: Node): Boolean

  /** The unique ID of the node.*/
  val UID: String

  override def hashCode(): Int = super.hashCode()
  override def equals(obj: Any): Boolean =
    obj match {case node: NetworkNode => node.UID == this.UID; case _ => super.equals(obj)}
}
