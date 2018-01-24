package it.unibo.scafi.simulation.gui.launcher.scalaFX


import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main() = branch(sense[Boolean]("obstacle")) {false} {channel(sense[Boolean]("source"), sense[Boolean]("destination"), 1)}
}