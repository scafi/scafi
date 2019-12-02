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
import it.unibo.scafi.renderer3d.util.Rendering3DUtils
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import org.scalafx.extras._
import scalafx.scene.Scene
import scalafx.scene.shape.Cylinder
import scala.collection.mutable.{Map => MutableMap}

private[manager] trait ConnectionManager {

  protected val mainScene: Scene

  //every connection is saved two times, so that it can be obtained from each node
  private final val connections = MutableMap[Node, MutableMap[Node, Cylinder]]()

  final def connect(node1UID: String, node2UID: String): Boolean = onFXAndWait {
    findNodes(node1UID, node2UID).fold(false)(nodes => connectNodes(nodes._1, nodes._2))
  }

  private final def findNodes(node1UID: String, node2UID: String): Option[(Node, Node)] =
    (mainScene.findNodeById(node1UID), mainScene.findNodeById(node2UID)) match {
      case (Some(nodeValue1), Some(nodeValue2)) => Option((nodeValue1, nodeValue2))
      case _ => None
    }

  private final def connectNodes(node1: Node, node2: Node): Boolean = {
    if(connections.contains(node1) && connections(node1).contains(node2)){
      false
    } else {
      val connection = createNodeConnection(node1, node2)
      connectNodesOneDirectional(node1, node2, connection)
      connectNodesOneDirectional(node2, node1, connection) //inverted the order of the nodes
      mainScene.getChildren.add(connection)
      true
    }
  }

  private final def connectNodesOneDirectional(originNode: Node, targetNode: Node, connection: Cylinder): Unit = {
    if(connections.contains(originNode)){ //the node already has some connections
      val innerMap = connections(originNode)
      innerMap(targetNode) = connection
    } else {
      connections(originNode) = MutableMap(targetNode -> connection)
    }
  }

  final def disconnect(node1UID: String, node2UID: String): Boolean = onFXAndWait {
    findNodes(node1UID, node2UID).fold(false)(nodes => disconnectNodes(nodes._1, nodes._2))
  }

  private final def disconnectNodes(node1: Node, node2: Node): Boolean =
    connections.get(node1).fold(false)(innerMap => {
      if(!innerMap.contains(node2)){
        false
      } else {
        mainScene.getChildren.remove(innerMap(node2)) //removes the line from the scene
        disconnectNodesOneDirectional(node1, node2)
        disconnectNodesOneDirectional(node2, node1)
        true
      }
    })

  private final def disconnectNodesOneDirectional(originNode: Node, targetNode: Node): Unit =
    connections(originNode).remove(targetNode)

  protected final def removeAllNodeConnections(node: Node): Unit = onFX {
    actOnAllNodeConnections(node, disconnectNodes(node, _))
  }

  private final def actOnAllNodeConnections(node: Node, action: Node => Unit): Unit =
    connections.get(node).fold()(_.keys.foreach(action(_)))

  protected final def updateNodeConnections(node: Node): Unit = onFX {
    actOnAllNodeConnections(node, updateConnection(node, _))
  }

  private final def updateConnection(node1: Node, node2: Node): Unit = {
    disconnectNodes(node1, node2)
    connectNodes(node1, node2)
  }

  private final def createNodeConnection(originNode: javafx.scene.Node, targetNode: javafx.scene.Node): Cylinder =
    originNode match {
      case origin: NetworkNode => targetNode match {
        case target: NetworkNode =>
          Rendering3DUtils.createLine(origin.getNodePosition, target.getNodePosition, 2)
      }
    }

  final def setConnectionsVisible(visible: Boolean): Unit =
    onFX(connections.flatMap(entry => entry._2.values).foreach(_.setVisible(visible))) //TODO: ottimizza
}