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

import javafx.embed.swing.JFXPanel
import org.scalafx.extras._
import scalafx.scene.Scene

final class NetworkRenderingPanel() extends JFXPanel
  with ConnectionManager with NodeManager with SelectionManager with SceneManager {

  override protected val mainScene: Scene = new Scene(createScene())
  this.setScene(mainScene)

  /**
   * Since the rest of the api returns immediately, this should be used to avoid the main loop being to fast and
   * flooding this object with requests, by blocking the main loop on this method.
   * */
  def blockUntilThreadIsFree(): JFXPanel = onFXAndWait {NetworkRenderingPanel.this}

}

object NetworkRenderingPanel {
  def apply(): NetworkRenderingPanel = new NetworkRenderingPanel()
}