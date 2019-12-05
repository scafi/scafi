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

package it.unibo.scafi.renderer3d.manager

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.Rendering3DUtils._
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.input.MouseEvent
import javafx.scene.shape.CullFace
import org.scalafx.extras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Shape3D
import scalafx.scene.{Camera, Scene}

private[manager] trait SelectionManager {
  this: NodeManager =>

  protected val mainScene: Scene
  private[this] val selectVolume =
    createBox(1, Color.color(0.2, 0.2, 0.8, 0.5), Point3D.Zero)
  private[this] var selectedNodes: List[NetworkNode] = List()
  private[this] var initialNode: Option[NetworkNode] = None

  selectVolume.setCullFace(CullFace.NONE)
  selectVolume.setVisible(false)

  protected final def setSelectionVolumeCenter(event: MouseEvent): Unit = onFX {
    val screenPosition = new Point2D(event.getScreenX, event.getScreenY)
    initialNode = Option(getAllNetworkNodes.minBy(node => node.getScreenPosition.distance(screenPosition)))
    deselectSelectedNodes()
  }

  protected final def startSelection(event: MouseEvent): Unit = onFX {
    if(initialNode.isDefined && !mainScene.getChildren.contains(selectVolume)){
      selectVolume.moveTo(initialNode.map(_.getNodePosition).getOrElse(Point3D.Zero))
      selectVolume.setScale(1)
      selectVolume.setVisible(true)
      mainScene.getChildren.add(selectVolume)
    }
  }

  private final def deselectSelectedNodes(): Unit = {
    selectedNodes.foreach(_.deselect())
    selectedNodes = List()
  }

  protected final def modifySelectionVolume(camera: Camera, event: MouseEvent): Unit = onFX {
    if(selectVolume.isVisible){
      val screenPosition = event.getScreenPosition
      val initialNodeScreenPosition = initialNode.map(_.getScreenPosition).getOrElse(Point2D.Zero)
      val cameraToNodeDistance = initialNode.map(_.getPosition).getOrElse(Point3D.Zero).distance(camera.getPosition)
      selectVolume.setScale(screenPosition.distance(initialNodeScreenPosition) * cameraToNodeDistance / 1000)
      selectVolume.lookAtOnXZPlane(camera.getPosition)
      updateSelectionIfNeeded(event)
    }
  }

  private final def updateSelectionIfNeeded(event: MouseEvent): Unit =
    if((event.getScreenX + event.getScreenY)%2 < 1){
      deselectSelectedNodes()
      selectedNodes = getIntersectingNetworkNodes(selectVolume)
      selectedNodes.foreach(_.select())
    }

  private final def getIntersectingNetworkNodes(shape: Shape3D): List[NetworkNode] =
    getAllNetworkNodes.filter(node => shape.getBoundsInParent.intersects(node.getBoundsInParent))

  protected final def endSelection(event: MouseEvent): Unit = onFX {
    if(selectVolume.isVisible){
      mainScene.getChildren.remove(selectVolume)
      selectVolume.setVisible(false)
    }
  }

  final def getSelectedNodesIDs: List[String] = onFXAndWait(selectedNodes.map(_.UID))
}
