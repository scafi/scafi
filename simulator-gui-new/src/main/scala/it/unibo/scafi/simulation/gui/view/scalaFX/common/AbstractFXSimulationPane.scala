package it.unibo.scafi.simulation.gui.view.scalaFX.common


import it.unibo.scafi.simulation.gui.view.SimulationView
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FXOutputPolicy

import scalafx.scene.layout.Pane

/**
  * define a generic interface on a fx simulation pane
  */
//noinspection AbstractValueInTrait

private [scalaFX] trait AbstractFXSimulationPane extends Pane with SimulationView {
  val drawer : FXOutputPolicy
  /**
    * get the nodes showed
    * @return the nodes drawed
    */
  def nodes : Map[ID,drawer.OUTPUT_NODE]
}
