package it.unibo.scafi.simulation.gui.view.scalaFX.common


import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FXDrawer
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.ZoomablePane
import it.unibo.scafi.simulation.gui.view.{GraphicsView, SimulationView}

import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.layout.Pane

/**
  * define a generic interface on a fx simulation pane
  */

trait AbstractFXSimulationPane[W <: World] extends ZoomablePane with GraphicsView with SimulationView[W] {
  val drawer : FXDrawer
  /**
    * get the nodes showed
    * @return the nodes drawed
    */
  def nodes : Map[W#ID,(drawer.OUTPUTNODE,Point2D)]

}
