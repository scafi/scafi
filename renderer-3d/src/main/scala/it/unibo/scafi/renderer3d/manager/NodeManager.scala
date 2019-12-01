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

import it.unibo.scafi.renderer3d.node.{NetworkNode, SimpleNetworkNode}
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.{Camera, Scene}

import scala.collection.mutable.{Map => MutableMap}

private[manager] trait NodeManager {
  this: ConnectionManager => //ConnectionManager has to also be mixed in with NodeManager

  private[this] final val networkNodesCache = MutableMap[String, NetworkNode]()
  protected val mainScene: Scene

  protected final def rotateAllNodeLabels(camera: Camera): Unit = onFX {
    networkNodesCache.values.foreach(_.rotateTextToCamera(camera))
  }

  final def addNode(position: Product3[Double, Double, Double], labelText: String, UID: String): Unit = onFX {
    val networkNode = SimpleNetworkNode(product3ToPoint3D(position), labelText, UID)
    networkNodesCache(UID) = networkNode
    mainScene.getChildren.add(networkNode)
  }

  private final def product3ToPoint3D(product: Product3[Double, Double, Double]): Point3D =
    new Point3D(product._1, product._2, product._3)

  final def removeNode(nodeUID: String): Boolean =
    onFXAndWait(findNodeAndExecuteAction(nodeUID, node => {
      networkNodesCache.remove(nodeUID)
      mainScene.getChildren.remove(node)
      removeAllNodeConnections(node) //using ConnectionManager
    }))

  protected final def findNodeAndExecuteAction(nodeUID: String, action: NetworkNode => Unit): Boolean =
      networkNodesCache.get(nodeUID).fold(false)(node => {action(node.toNetworkNode); true})

  final def moveNode(nodeUID: String, position: Product3[Double, Double, Double]): Boolean = //TODO: controlla che sia ottimizzato
    onFXAndWait(findNodeAndExecuteAction(nodeUID, { node =>
      node.moveTo(product3ToPoint3D(position))
      updateNodeConnections(node) //using ConnectionManager
    }))

  final def updateNodeText(nodeUID: String, text: String): Boolean =
    onFXAndWait(findNodeAndExecuteAction(nodeUID, _.updateText(text)))

  final def setNodeColor(nodeUID: String, color: java.awt.Color): Boolean =
    onFXAndWait(findNodeAndExecuteAction(nodeUID, _.setColor(color)))

  protected final def getAllNetworkNodes: List[NetworkNode] = networkNodesCache.values.toList
}