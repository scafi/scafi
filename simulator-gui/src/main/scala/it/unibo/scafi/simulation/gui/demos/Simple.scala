package it.unibo.scafi.simulation.gui.demos

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi.Actions._
import it.unibo.scafi.simulation.gui.launcher.WorldConfig
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXDrawer, StandardFXDrawer}
object Test extends App {
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher._
  program = classOf[Simple]
  drawer = StandardFXDrawer
  nodes = 1000
  boundary = Some(Rectangle(1920,1080))
  radius = 80
  neighbourRender = true
  /*drawer = FastFXDrawer
  nodes = 10000
  boundary = Some(Rectangle(500,500))
  radius = 8
  neighbourRender = false*/
  actions = generalaction :: actions
  launch()
}
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  import it.unibo.scafi.simulation.gui.launcher.SensorName._
  override def main() = branch(sense[Boolean](sens3.name)) {false} {channel(sense[Boolean](sens1.name), sense[Boolean](sens2.name), 1)}
}