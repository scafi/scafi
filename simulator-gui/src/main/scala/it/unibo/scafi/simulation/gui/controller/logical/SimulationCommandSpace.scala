package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.configuration.CommandSpace
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * a simulation command space used to describe command to modify simulation
  */
trait SimulationCommandSpace extends CommandSpace {
  /**
    * @return the simulation of this oommand space
    */
  def simulation : ExternalSimulation[_ <: AggregateWorld]

  /**
    * stop current simulation
    */
  case object StopSimulation extends Command {

    override def make(): Unit = simulation.stop()

    override def unmake(): Unit = simulation.continue()

    override def buildFromString(string: String): Option[Command] = None
  }

  /**
    * continue a simulation stopped
    */
  case object ContinueSimulation extends Command {

    override def make(): Unit = simulation.continue()

    override def unmake(): Unit = simulation.stop()

    override def buildFromString(string: String): Option[Command] = None
  }

  override def fromString(string: String): Option[Command] = None
}
