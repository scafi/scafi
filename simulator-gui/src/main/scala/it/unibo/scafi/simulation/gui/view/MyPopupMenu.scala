package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/**
  * This class represent the SoimulationPanel pop menu
  * that contains all possible action and observation
  *
  * Created by Varini on 11/11/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class MyPopupMenu() extends JPopupMenu {
  final private val controller: Controller = Controller.getIstance
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
        observation.setEnabled(false)
      }
    }

    if (nameObservation == "SENSOR")
      observation.addActionListener(actlist)
    else
      observation.addActionListener(actList1)

    observations.add(observation)
  }
}