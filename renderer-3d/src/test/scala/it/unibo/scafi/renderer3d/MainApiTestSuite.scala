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

import it.unibo.scafi.renderer3d.manager.NetworkRenderingPanel
import javax.swing.SwingUtilities
import org.scalatest.FunSuite

class MainApiTestSuite extends FunSuite {

  private def getFixture = new {
    val NODE_COUNT = 200
    val networkRenderer: NetworkRenderingPanel = NetworkRenderingPanel()
    RunnableTestExample.addNodes(networkRenderer, NODE_COUNT)
    RunnableTestExample.connectNodes(networkRenderer, NODE_COUNT)
  }

  test("The connection API should connect and disconnect nodes as expected") {
    testOnSwingThread(assertConnections)
  }

  test("The node API should create, remove and move nodes as expected") {
    testOnSwingThread(assertNodes)
  }

  test("The selection API should not be selecting nodes without user interaction") {
    testOnSwingThread(assertSelection)
  }

  private def testOnSwingThread(action: NetworkRenderingPanel => Unit): Unit = {
    SwingUtilities.invokeAndWait(() => {
      val fixture = getFixture
      action(fixture.networkRenderer)
    })
  }

  private def assertConnections(renderingPanel: NetworkRenderingPanel): Unit = {
    assert(renderingPanel.getNodesConnectedToNode("50").getOrElse(Set()) == Set("49", "51"))
    assert(renderingPanel.disconnect("1", "2"))
    assertNeighbours("1", "2", connected = false, renderingPanel)
    assertNeighbours("2", "1", connected = false, renderingPanel)
    assert(renderingPanel.connect("1", "2"))
    assertNeighbours("1", "2", connected = true, renderingPanel)
    assertNeighbours("2", "1", connected = true, renderingPanel)
  }

  private def assertNodes(renderingPanel: NetworkRenderingPanel): Unit = {
    assert(renderingPanel.addNode((0d, 0d, 0d), "201"))
    assert(renderingPanel.removeNode("201"))
    assert(renderingPanel.getNodePosition("201").isEmpty)
    assertPositionChange( renderingPanel)
  }

  private def assertNeighbours(node1Id: String, node2Id: String, connected: Boolean,
                               renderingPanel: NetworkRenderingPanel): Unit = {
    val neighbours = renderingPanel.getNodesConnectedToNode(node1Id)
    if(connected){
      assert(neighbours.isDefined && neighbours.getOrElse(Set()).contains(node2Id))
    } else {
      assert(neighbours.isEmpty || !neighbours.getOrElse(Set()).contains(node2Id))
    }
  }

  private def assertPositionChange(renderingPanel: NetworkRenderingPanel,nodeId: String = "10"): Unit = {
    val position = renderingPanel.getNodePosition(nodeId)
    assert(renderingPanel.moveNode(nodeId, (0.5, 0.75, 0.25)))
    val newPosition = renderingPanel.getNodePosition(nodeId)
    assert(position != newPosition && newPosition == Option(0.5d, 0.75d, 0.25d))
  }

  private def assertSelection(renderingPanel: NetworkRenderingPanel): Unit = {
    assert(renderingPanel.getSelectedNodesIDs.isEmpty)
    assert(!renderingPanel.isAttemptingSelection)
  }

}
