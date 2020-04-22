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

import java.awt.{Color, Image}

import javafx.embed.swing.JFXPanel

/** Interface of the main entry point of this module. This renders all the nodes, the connections, etc.
 * Users of this module should use only this interface to interact with renderer-3d, as it offers all the main APIs
 * needed for adding, removing and moving nodes and connections, handling the selected nodes, etc. */
trait NetworkRenderer3D extends JFXPanel{

  //** connections API **
  /** See [[it.unibo.scafi.renderer3d.manager.connection.ConnectionManager#setConnectionsColor(java.awt.Color)]] */
  def setConnectionsColor(color: Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.connection.ConnectionManager#connect(int, int)]] */
  def connect(node1UID: Int, node2UID: Int): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.connection.ConnectionManager#disconnect(int, int)]] */
  def disconnect(node1UID: Int, node2UID: Int): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.connection.ConnectionManager#toggleConnections()]] */
  def toggleConnections(): Unit

  //** nodes API **
  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#enableNodeFilledSphere(int, boolean)]] */
  def enableNodeFilledSphere(nodeUID: Int, enable: Boolean): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setSpheresRadius(double, double)]] */
  def setSpheresRadius(seeThroughSpheresRadius: Double, filledSpheresRadius: Double): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#addNode(scala.Product3, int)]] */
  def addNode(position: Product3[Double, Double, Double], UID: Int): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#removeNode(int)]] */
  def removeNode(nodeUID: Int): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#moveNode(int, scala.Product3, boolean)]] */
  def moveNode(nodeUID: Int, position: Product3[Double, Double, Double], showDirection: Boolean): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#stopShowingNodeMovement(int)]] */
  def stopShowingNodeMovement(nodeUID: Int): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setNodeText(int, java.lang.String)]] */
  def setNodeText(nodeUID: Int, text: String): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setNodeTextAsUIPosition(int, scala.Function1)]] */
  def setNodeTextAsUIPosition(nodeUID: Int, positionFormatter: Product2[Double, Double] => String): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setNodeColor(int, java.awt.Color)]] */
  def setNodeColor(nodeUID: Int, color: java.awt.Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setNodesColor(java.awt.Color)]] */
  def setNodesColor(defaultColor: java.awt.Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setFilledSpheresColor(java.awt.Color)]] */
  def setFilledSpheresColor(color: java.awt.Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#setNodesScale(double)]] */
  def setNodesScale(scale: Double): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#increaseFontSize()]] */
  def increaseFontSize(): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.node.NodeManager#decreaseFontSize()]] */
  def decreaseFontSize(): Unit

  //** scene API **
  /** See [[it.unibo.scafi.renderer3d.manager.scene.SceneManager#setSceneSize(double)]] */
  def setSceneSize(sceneSize: Double): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.scene.SceneManager#setBackgroundImage(java.awt.Image)]] */
  def setBackgroundImage(image: Image): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.scene.SceneManager#setBackgroundColor(java.awt.Color)]] */
  def setBackgroundColor(color: Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.scene.SceneManager#resetScene()]] */
  def resetScene(): Unit

  //** selection API **
  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#setActionOnMovedNodes(scala.Function2)]] */
  def setActionOnMovedNodes(action: (Set[Int], Product3[Double, Double, Double]) => Unit): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#getSelectedNodesIDs()]] */
  def getSelectedNodesIDs: Set[Int]

  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#getInitialSelectedNodeId()]] */
  def getInitialSelectedNodeId: Option[Int]

  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#setCurrentSelectionColor(java.awt.Color)]] */
  def setCurrentSelectionColor(color: java.awt.Color): Unit

  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#isAttemptingSelection()]] */
  def isAttemptingSelection: Boolean

  /** See [[it.unibo.scafi.renderer3d.manager.selection.SelectionManager#setSelectionColor(java.awt.Color)]] */
  def setSelectionColor(color: java.awt.Color): Unit

  //** rest of the API **
  /** See [[it.unibo.scafi.renderer3d.manager.NetworkRendering3DPanel#blockUntilThreadIsFree()]] */
  def blockUntilThreadIsFree(): JFXPanel
}
