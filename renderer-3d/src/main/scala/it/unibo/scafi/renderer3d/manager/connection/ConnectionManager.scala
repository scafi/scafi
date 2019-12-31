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

package it.unibo.scafi.renderer3d.manager.connection

import java.awt.Color

import com.typesafe.scalalogging.Logger
import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.scene.SceneManager
import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.Rendering3DUtils
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.scene.Node
import org.fxyz3d.shapes.primitives.FrustumMesh
import org.scalafx.extras._
import scalafx.scene.Group

import scala.collection.mutable.{Map => MutableMap}

/** Trait that contains some of the main API of the renderer-3d module: the methods that create or modify connections.*/
private[manager] trait ConnectionManager {
  this: NodeManager with SceneManager => //NodeManager and SceneManager have to also be mixed in with ConnectionManager

  private type Line = FrustumMesh //instead of Cylinder, for performance reasons
  protected val connectionGroup = new Group() //implementations have to add this to the main scene
  private[this] var connectionsColor = Color.BLACK
  private[this] final val connections = MutableMap[String, MutableMap[String, Line]]() //each line is saved 2 times
  private[this] var connectionsVisible = true
  private[this] val logger = Logger("ConnectionManager")

  /** Sets the color that every connection will have.
   * @param color the chosen color
   * @return Unit, since it has the side effect of changing the connection color */
  final def setConnectionsColor(color: Color): Unit = onFX {
    connectionsColor = color
    getAllConnections.foreach(_.setColor(color))
  }

  private final def getAllConnections: Set[Line] = connections.flatMap(entry => entry._2.values).toSet

  /** Connects the two specified nodes if not already connected, adding the connection to the scene.
   * @param node1UID the id of the first node to connect
   * @param node2UID the id of the second node to connect
   * @return Unit, since it has the side effect of connecting the two nodes. */
  final def connect(node1UID: String, node2UID: String): Unit =
    onFX {findNodes(node1UID, node2UID).fold()(nodes => connectNodes(nodes._1, nodes._2))}

  private final def findNodes(node1UID: String, node2UID: String): Option[(NetworkNode, NetworkNode)] =
    (findNode(node1UID), findNode(node2UID)) match {
      case (Some(nodeValue1), Some(nodeValue2)) => Option((nodeValue1, nodeValue2))
      case _ => logger.error("Can't find nodes " + node1UID + " and " + node2UID); None
    }

  private final def connectNodes(node1: Node, node2: Node): Unit = {
    val (node1ID, node2ID) = (node1.getId, node2.getId)
    if(connections.contains(node1ID) && connections(node1ID).contains(node2ID)){
      logger.error("Nodes " + node1ID + " and " + node2ID + " are already connected")
    } else {
      val connection = createNodeConnection(node1, node2)
      connectNodesOneDirectional(node1ID, node2ID, connection)
      connectNodesOneDirectional(node2ID, node1ID, connection) //inverted the order of the nodes
      connectionGroup.getChildren.add(connection)
    }
  }

  private final def connectNodesOneDirectional(originNodeID: String, targetNodeID: String, connection: Line): Unit = {
    if(connections.contains(originNodeID)){ //the node already has some connections
      val innerMap = connections(originNodeID)
      innerMap(targetNodeID) = connection
    } else {
      connections(originNodeID) = MutableMap(targetNodeID -> connection)
    }
  }

  /** Disconnects the two specified nodes if not already disconnected, removing the connection from the scene.
   * @param node1UID the id of the first node to disconnect
   * @param node2UID the id of the second node to disconnect
   * @return Unit, since it has the side effect of disconnecting the two nodes. */
  final def disconnect(node1UID: String, node2UID: String): Unit = onFX {
    connections.get(node1UID).fold()(innerMap => {
      if(!innerMap.contains(node2UID)){
        logger.error("Nodes " + node1UID + " and " + node2UID + " are not already connected")
      } else {
        connectionGroup.getChildren.remove(innerMap(node2UID)) //removes the line from the scene
        disconnectNodesOneDirectional(node1UID, node2UID)
        disconnectNodesOneDirectional(node2UID, node1UID)
      }
    })}

  private final def disconnectNodesOneDirectional(originNodeID: String, targetNodeID: String): Unit =
    connections(originNodeID).remove(targetNodeID)

  protected final def removeAllNodeConnections(nodeID: String): Unit =
    onFX {actOnAllNodeConnections(nodeID, disconnect(nodeID, _))}

  private final def actOnAllNodeConnections(nodeID: String, action: String => Unit): Unit =
    connections.get(nodeID).fold()(_.keys.foreach(action(_)))

  protected final def updateNodeConnections(nodeID: String): Unit =
    onFX {actOnAllNodeConnections(nodeID, updateConnection(nodeID, _))}

  private final def updateConnection(node1ID: String, node2ID: String): Unit = {
    val line = connections(node1ID)(node2ID)
    findNodes(node1ID, node2ID).fold()(nodes =>
      Rendering3DUtils.connectLineToPoints(line, nodes._1.getNodePosition, nodes._2.getNodePosition))
  }

  private final def createNodeConnection(originNode: javafx.scene.Node, targetNode: javafx.scene.Node): Line =
    (originNode, targetNode) match {case (origin: NetworkNode, target: NetworkNode) =>
      val points = (origin.getNodePosition, target.getNodePosition)
      Rendering3DUtils.createLine(points, connectionsVisible, connectionsColor, sceneScaleMultiplier/5)
    }

  /** Toggles the connections on or off, making them visible or invisible.
   * @return Unit, since it has the side effect of making the connections visible or invisible */
  final def toggleConnections(): Unit = onFX {
    connectionsVisible = !connectionsVisible
    setConnectionsVisible(connectionsVisible)
  }

  private final def setConnectionsVisible(setVisible: Boolean): Unit =
    if(setVisible) mainScene.getChildren.add(connectionGroup) else mainScene.getChildren.remove(connectionGroup)
}