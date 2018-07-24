package it.unibo.scafi.simulation.gui.view.scalaFX.common


import it.unibo.scafi.simulation.gui.view.SimulationView
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FXDrawer
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.ZoomablePane

import scalafx.geometry.Point2D

/**
  * define a generic interface on a fx simulation pane
  */

trait AbstractFXSimulationPane extends ZoomablePane with SimulationView {
  val drawer : FXDrawer
  /**
    * get the nodes showed
    * @return the nodes drawed
    */
  def nodes : Map[ID,(drawer.OUTPUTNODE,Point2D)]

}
