package it.unibo.scafi.simulation.gui.demos

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig
object Test extends App {
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher._
  program = classOf[Simple]
  nodes = 10000
  maxPoint = 1000
  radius = 50
  neighbourRender = true
  launch()
}
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width
  import WorldConfig._
  override def main() = branch(sense[Boolean](obstacle.name)) {false} {channel(sense[Boolean](source.name), sense[Boolean](destination.name), 1)}
}