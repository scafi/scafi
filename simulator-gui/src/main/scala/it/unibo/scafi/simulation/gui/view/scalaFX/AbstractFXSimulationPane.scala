package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.ZoomablePane
import it.unibo.scafi.simulation.gui.view.{GraphicsView, SimulationView}

import scalafx.geometry.Point2D
import scalafx.scene.Node

/**
  * define a generic interface on a fx simulation pane
  */

trait AbstractFXSimulationPane extends ZoomablePane with GraphicsView with SimulationView {
  def nodes : Map[World#ID,(Node,Point2D)]
}
