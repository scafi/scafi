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

import it.unibo.scafi.renderer3d.manager.NetworkRenderingPanel
import javax.swing.{JFrame, SwingUtilities}
import scala.util.Random

private[renderer3d] object RunnableTestExample extends App {
  private val NODE_COUNT = 200
  private val NODE_BRIGHTNESS = 50

  SwingUtilities.invokeLater(() => {
    val frame = new JFrame()
    val networkRenderer = NetworkRenderingPanel()
    frame.add(networkRenderer, BorderLayout.CENTER)
    frame.setVisible(true)

    addNodes(networkRenderer, NODE_COUNT)
    connectNodes(networkRenderer, NODE_COUNT)
    setNodesLabel(networkRenderer)
    testAPI(networkRenderer)
  })

  def addNodes(networkRenderer: NetworkRenderingPanel, nodeCount: Int): Unit = (1 to nodeCount).foreach(index =>
    networkRenderer.addNode((getRandomDouble, getRandomDouble, getRandomDouble), index.toString))

  private def getRandomDouble: Double = {
    val MAX_VALUE = 10000
    Random.nextInt(MAX_VALUE).toDouble
  }

  private def testAPI(networkRenderer: NetworkRenderingPanel): Unit = {
    testNodeAPI(networkRenderer)
    testConnectionsAPI(networkRenderer)
    testSelectionAPI(networkRenderer)
  }

  private def testNodeAPI(networkRenderer: NetworkRenderingPanel): Unit = {
    moveNodesRandomly(networkRenderer)
    networkRenderer.increaseFontSize()
    networkRenderer.decreaseFontSize()
    networkRenderer.removeNode((NODE_COUNT-1).toString)
    networkRenderer.setNodeTextAsUIPosition((NODE_COUNT-2).toString, {case (x: Double, y: Double) => x + " " + y})
    networkRenderer.setNodeColor((NODE_COUNT-2).toString, Color.orange)
    networkRenderer.setNodesColor(new Color(NODE_BRIGHTNESS, NODE_BRIGHTNESS, NODE_BRIGHTNESS))
    networkRenderer.setNodeColor("1", Color.blue)
  }

  private def testConnectionsAPI(networkRenderer: NetworkRenderingPanel): Unit = {
    networkRenderer.toggleConnections()
    networkRenderer.toggleConnections()
    networkRenderer.setConnectionsColor(Color.green)
    networkRenderer.disconnect("1", "2")
  }

  private def testSelectionAPI(networkRenderer: NetworkRenderingPanel): Unit =
    networkRenderer.addKeyListener(new KeyListener {
    override def keyTyped(keyEvent: KeyEvent): Unit = ()
    override def keyPressed(keyEvent: KeyEvent): Unit = ()

    override def keyReleased(keyEvent: KeyEvent): Unit =
      if(keyEvent.getKeyCode == KeyEvent.VK_1) {
        networkRenderer.setModifiedNodesColor(Color.YELLOW)
        println("Selected nodes: " + networkRenderer.getSelectedNodesIDs)
      }
  })

  def connectNodes(renderingPanel: NetworkRenderingPanel, nodeCount: Int): Unit =
    (1 to nodeCount).foreach(index => renderingPanel.connect(index.toString, (index + 1).toString))

  private def moveNodesRandomly(renderingPanel: NetworkRenderingPanel): Unit =
    (0 to NODE_COUNT).foreach(index => renderingPanel.moveNode(index.toString, getRandomPosition))

  private def getRandomPosition: (Double, Double, Double) = (getRandomDouble, getRandomDouble, getRandomDouble)

  private def setNodesLabel(networkRenderer: NetworkRenderingPanel): Unit =
    (0 to NODE_COUNT).foreach(index => networkRenderer.setNodeText(index.toString, index.toString))
}
