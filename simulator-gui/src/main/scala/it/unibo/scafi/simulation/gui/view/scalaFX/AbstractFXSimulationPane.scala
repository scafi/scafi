package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.{FXDrawer, ZoomablePane}
import it.unibo.scafi.simulation.gui.view.{GraphicsView, SimulationView}

import scalafx.geometry.Point2D
import scalafx.scene.Node

/**
  * define a generic interface on a fx simulation pane
  */

trait AbstractFXSimulationPane[W <: World] extends ZoomablePane with GraphicsView with SimulationView[W] {
  /**
    * get the nodes showed
    * @return the nodes drawed
    */
  def nodes : Map[W#ID,(Node,Point2D)]

  /**
    * define a drawer for the scene
    * @return the fxdrawer
    */
  def drawer : FXDrawer
}
