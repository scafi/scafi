package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.configuration.CommandSpace
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

object SimulationCommandSpace extends CommandSpace {
  var simulation : Option[ExternalSimulation[_ <: AggregateWorld]] = None

  /**
    * stop current simulation
    */
  case object StopSimulation extends Command {

    override def make(): Unit = simulation.get.stop()

    override def unmake(): Unit = simulation.get.continue()

    override def buildFromString(string: String): Option[Command] = None
  }

  case object ContinueSimulation extends Command {

    override def make(): Unit = simulation.get.continue()

    override def unmake(): Unit = simulation.get.stop()

    override def buildFromString(string: String): Option[Command] = None
  }

  override def fromString(string: String): Option[Command] = None
}
