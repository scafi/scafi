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

import java.awt.Color
import it.unibo.scafi.renderer3d.node.{NetworkNode, SimpleNetworkNode}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.{Camera, Scene}

import scala.collection.mutable.{Map => MutableMap}

private[manager] trait NodeManager {
  this: ConnectionManager => //ConnectionManager has to also be mixed in with NodeManager

  private[this] final val networkNodes = MutableMap[String, NetworkNode]()
  private[this] var nodeLabelsScale = 1d
  private[this] val BRIGHTNESS = 50 //out of 255
  private[this] var nodesColor = new Color(BRIGHTNESS, BRIGHTNESS, BRIGHTNESS)
  private[this] var selectionColor = java.awt.Color.red
  private[this] var positionThatLabelsFace = Point3D.Zero
  private[this] var nodesRadius = 0d
  protected val mainScene: Scene

  protected final def rotateNodeLabelsIfNeeded(camera: Camera): Unit = onFX {
    val cameraPosition = camera.getPosition
    if(cameraPosition.distance(positionThatLabelsFace) > 3000){
      networkNodes.values.foreach(_.rotateTextToCamera(cameraPosition))
      positionThatLabelsFace = cameraPosition
    }
  }

  final def drawNodesRadius(draw: Boolean, radius: Double): Unit = onFX {
    nodesRadius = if(draw && radius > 0) radius else 0
    //TODO
  }

  final def addNode(position: Product3[Double, Double, Double], UID: String): Boolean = onFXAndWait {
    val nodeAlreadyExists = networkNodes.contains(UID)
    if(!nodeAlreadyExists){
      val networkNode = SimpleNetworkNode(product3ToPoint3D(position), UID, nodesColor.toScalaFx, nodeLabelsScale)
      networkNode.setSelectionColor(selectionColor.toScalaFx)
      networkNodes(UID) = networkNode
      mainScene.getChildren.add(networkNode)
    }
    !nodeAlreadyExists
  }

  private final def product3ToPoint3D(product: Product3[Double, Double, Double]): Point3D =
    new Point3D(product._1, product._2, product._3)

  final def removeNode(nodeUID: String): Boolean = findNodeAndExecuteAction(nodeUID, node => {
      networkNodes.remove(nodeUID)
      mainScene.getChildren.remove(node)
      removeAllNodeConnections(node) //using ConnectionManager
    })

  private final def findNodeAndExecuteAction(nodeUID: String, action: NetworkNode => Unit): Boolean =
    onFXAndWait(findNode(nodeUID).fold(false)(node => {action(node.toNetworkNode); true}))

  protected final def findNode(nodeUID: String): Option[NetworkNode] = networkNodes.get(nodeUID)

  final def moveNode(nodeUID: String, position: Product3[Double, Double, Double]): Boolean =
    findNodeAndExecuteAction(nodeUID, { node =>
      node.moveNodeTo(product3ToPoint3D(position))
      updateNodeConnections(node) //using ConnectionManager
    })

  final def getNodePosition(nodeUID: String): Option[Product3[Double, Double, Double]] =
    onFXAndWait(networkNodes.get(nodeUID).map((node: NetworkNode) => point3DToProduct3(node.getNodePosition)))

  private final def point3DToProduct3(point: Point3D): Product3[Double, Double, Double] =
    (point.x, point.y, point.z)

  final def setNodeText(nodeUID: String, text: String): Boolean =
    findNodeAndExecuteAction(nodeUID, _.setText(text))

  final def setNodeTextAsUIPosition(nodeUID: String, positionFormatter: Product2[Double, Double] => String): Boolean =
    findNodeAndExecuteAction(nodeUID, node => {
      val position = node.getScreenPosition
      node.setText(positionFormatter((position.x, position.y)))
    })

  final def setNodeColor(nodeUID: String, color: java.awt.Color): Boolean =
    findNodeAndExecuteAction(nodeUID, _.setNodeColor(color.toScalaFx))

  final def setNodesColor(color: java.awt.Color): Unit = onFX {
    nodesColor = color
    networkNodes.values.foreach(_.setNodeColor(color.toScalaFx))
  }

  protected final def getAllNetworkNodes: Set[NetworkNode] = networkNodes.values.toSet

  final def increaseFontSize(): Unit = updateLabelSize(0.1)

  final def decreaseFontSize(): Unit = updateLabelSize(-0.1)

  private final def updateLabelSize(sizeDifference: Double): Unit = onFX {
    val MIN_SCALE = 0.5
    val MAX_SCALE = 1.5
    nodeLabelsScale = RichMath.clamp(nodeLabelsScale + sizeDifference, MIN_SCALE, MAX_SCALE)
    networkNodes.values.foreach(_.setLabelScale(nodeLabelsScale))
  }

  final def setSelectionColor(color: java.awt.Color): Unit = onFX {
    selectionColor = color
    getAllNetworkNodes.foreach(_.setSelectionColor(color.toScalaFx))
  }
}