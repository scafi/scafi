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

import it.unibo.scafi.renderer3d.fps_counter.FPSCounter
import it.unibo.scafi.renderer3d.manager.connection.ConnectionManager
import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.scene.SceneManager
import it.unibo.scafi.renderer3d.manager.selection.SelectionManager
import javafx.embed.swing.JFXPanel
import org.scalafx.extras._
import scalafx.application.Platform
import scalafx.scene.Scene

/**
 * Main entry point of this module. This class extends JFXPanel (because NetworkRenderer does) so that it can be used
 * as a normal JPanel inside Swing. This panel is the main one, it renders all the nodes, the connections, etc.
 * This class offers all the main APIs needed for adding, removing and moving nodes and connections, handling the
 * selected nodes, etc.
 * Developers can also enable a FPS counter to check performance (off by default).
 * */
final class NetworkRendering3DPanel() extends NetworkRenderer3D with ConnectionManager with NodeManager
  with SelectionManager with SceneManager{

  private val SHOW_FPS_COUNTER = false //use this when you want to check performance
  override protected val mainScene: Scene = new Scene(createScene())

  mainScene.getChildren.add(connectionGroup)
  this.setScene(mainScene)
  setupCameraAndListeners()
  Platform.runLater(stopMovingOnFocusLoss(mainScene.getWindow)) //runLater is needed, otherwise getWindow would be null
  if(SHOW_FPS_COUNTER) FPSCounter.addToScene(mainScene)

  /**
   * This method adds a small job to the ones that the javaFx thread has to run and it waits for its result.
   * Therefore, it blocks the calling thread until the javaFx thread is free enough to run the new job.
   * Since the rest of the api returns immediately, this should be used to avoid the main loop being too fast and
   * flooding the javaFx thread with requests, by calling this method in the main loop.
   * */
  def blockUntilThreadIsFree(): JFXPanel = onFXAndWait {NetworkRendering3DPanel.this}

}

object NetworkRendering3DPanel {
  def apply(): NetworkRendering3DPanel = new NetworkRendering3DPanel()
}