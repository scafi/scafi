package it.unibo.scafi.simulation.gui

import java.awt.event.{ActionEvent, ActionListener}

/**
  * @author Roberto Casadei
  *
  */

package object view {
  implicit def toActionListener(f: ActionEvent => Unit) = new ActionListener {
    def actionPerformed(e: ActionEvent) { f(e) }
  }
}
