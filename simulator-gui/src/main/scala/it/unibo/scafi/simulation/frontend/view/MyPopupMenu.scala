/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import it.unibo.scafi.simulation.frontend.controller.Controller

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
