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

package it.unibo.scafi.simulation.gui.view

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller

/**
  * This class represent the SoimulationPanel pop menu
  * that contains all possible action and observation
  */
class MyPopupMenu() extends JPopupMenu {
  final private val controller: Controller = Controller.getInstance
  final private val observations: JMenu = new JMenu("Observe")
  final private val actions: JMenu = new JMenu("Actions")
  val clear: JMenuItem = new JMenuItem("Clear")

  clear.addActionListener((e:ActionEvent) => {
    controller.clearSimulation()
  })
  add(clear)
  addSeparator()
  add(observations)
  addSeparator()
  add(actions)
  observations.setEnabled(false)
  actions.setEnabled(false)

  def addAction(nameAction: String, actlist: ActionListener) {
    val action: JMenuItem = new JMenuItem(nameAction)
    action.addActionListener(actlist)
    actions.add(action)
  }

  def addObservation(nameObservation: String, actlist: ActionListener) {
    val observation: JMenuItem = new JMenuItem(nameObservation)
    val actList1: ActionListener = new ActionListener() {
      def actionPerformed(e: ActionEvent) {
        actlist.actionPerformed(e)
        var i: Int = 0
        while (i < observations.getItemCount) {
            observations.getItem(i).setEnabled(true)
            i += 1; i - 1
        }
        //observation.setEnabled(false)
      }
    }

    if (nameObservation == "SENSOR")
      observation.addActionListener(actlist)
    else
      observation.addActionListener(actList1)

    observations.add(observation)
  }
}
