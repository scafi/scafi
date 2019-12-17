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

package it.unibo.scafi.renderer3d

import java.awt.event.{KeyEvent, KeyListener}
import java.awt.{BorderLayout, Color}
import com.typesafe.scalalogging.Logger
import it.unibo.scafi.renderer3d.manager.{NetworkRenderer3D, NetworkRendering3DPanel}
import javax.swing.{JFrame, SwingUtilities, WindowConstants}

import scala.util.Random

/**
 * Example usage of the main API of this module, provided by [[NetworkRenderer3D]]. This class creates some nodes,
 * connects each one to the previous one and to the next one, also moves them around randomly at the start, etc. It
 * also calls many methods of the main API to test that they actually work.
 * This example can be used to quickly check for regressions in the renderer-3d module, by running it and checking if
 * the scene looks and behaves as usual:
 *  -the second-last node should not exist
 *  -the node before the second-last should show its 2D position instead of its index value, also it should be orange
 *  -the first node should be blue
 *  -the connections, nodes and labels should be visible
 *  -the connections should be green
 *  -nodes 1 and 2 should be disconnected
 *  -selecting some nodes and pressing a keyboard number between 1 and 4 should print the selected nodes to the console
 *    and should set their color to yellow
 *  -the background color should be light gray
 * */
private[renderer3d] object RunnableTestExample extends App {
  private val SCENE_SIZE = 1000
  private val FRAME_WIDTH = 800
  private val FRAME_HEIGHT = 600
  private val NODE_COUNT = 400
  private val NODE_BRIGHTNESS = 50
  private val logger = Logger("RunnableTestExample")

  SwingUtilities.invokeLater(() => {
    val frame = new JFrame()
    val networkRenderer: NetworkRenderer3D = NetworkRendering3DPanel()
    networkRenderer.setSceneSize(SCENE_SIZE)
    frame.add(networkRenderer, BorderLayout.CENTER)
    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

    testSceneAPI(networkRenderer) //this needs to be done first because it resets the scene
    addNodes(networkRenderer, NODE_COUNT)
    connectNodes(networkRenderer, NODE_COUNT)
    setNodesLabel(networkRenderer)
    testAPI(networkRenderer)
  })

  private def addNodes(networkRenderer: NetworkRenderer3D, nodeCount: Int): Unit = (1 to nodeCount).foreach(index =>
    networkRenderer.addNode((getRandomDouble, getRandomDouble, getRandomDouble), index.toString))

  private def getRandomDouble: Double = Random.nextInt(SCENE_SIZE).toDouble - SCENE_SIZE/2

  private def testAPI(networkRenderer: NetworkRenderer3D): Unit = {
    testNodeAPI(networkRenderer)
    testConnectionsAPI(networkRenderer)
    testSelectionAPI(networkRenderer)
  }

  private def testSceneAPI(networkRenderer: NetworkRenderer3D): Unit = {
    networkRenderer.resetScene()
    networkRenderer.setBackgroundColor(Color.LIGHT_GRAY)
  }

  private def testNodeAPI(networkRenderer: NetworkRenderer3D): Unit = {
    moveNodesRandomly(networkRenderer)
    networkRenderer.increaseFontSize()
    networkRenderer.decreaseFontSize()
    networkRenderer.removeNode((NODE_COUNT-1).toString)
    networkRenderer.setNodeTextAsUIPosition((NODE_COUNT-2).toString, {case (x: Double, y: Double) => x + " " + y})
    networkRenderer.setNodesColor(new Color(NODE_BRIGHTNESS, NODE_BRIGHTNESS, NODE_BRIGHTNESS))
    networkRenderer.setNodeColor((NODE_COUNT-2).toString, Color.orange)
    networkRenderer.setNodeColor("1", Color.blue)
  }

  private def testConnectionsAPI(networkRenderer: NetworkRenderer3D): Unit = {
    networkRenderer.toggleConnections()
    networkRenderer.toggleConnections()
    networkRenderer.setConnectionsColor(Color.green)
    networkRenderer.disconnect("1", "2")
  }

  private def testSelectionAPI(networkRenderer: NetworkRenderer3D): Unit =
    networkRenderer.addKeyListener(new KeyListener {
    override def keyTyped(keyEvent: KeyEvent): Unit = ()
    override def keyPressed(keyEvent: KeyEvent): Unit = ()

    override def keyReleased(keyEvent: KeyEvent): Unit =
      if(keyEvent.getKeyCode == KeyEvent.VK_1) {
        networkRenderer.setCurrentSelectionColor(Color.YELLOW)
        logger.info("Selected nodes: " + networkRenderer.getSelectedNodesIDs)
      }
  })

  private def connectNodes(renderingPanel: NetworkRenderer3D, nodeCount: Int): Unit =
    (1 until nodeCount).foreach(index => renderingPanel.connect(index.toString, (index + 1).toString))

  private def moveNodesRandomly(renderingPanel: NetworkRenderer3D): Unit =
    (1 to NODE_COUNT).foreach(index => renderingPanel.moveNode(index.toString, getRandomPosition))

  private def getRandomPosition: (Double, Double, Double) = (getRandomDouble, getRandomDouble, getRandomDouble)

  private def setNodesLabel(networkRenderer: NetworkRenderer3D): Unit =
    (1 to NODE_COUNT).foreach(index => networkRenderer.setNodeText(index.toString, index.toString))
}
