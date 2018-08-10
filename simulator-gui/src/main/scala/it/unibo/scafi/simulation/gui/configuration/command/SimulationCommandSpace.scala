package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.CommandSpace.illegalOrder
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command.{CommandDescription, CommandResult, Fail, Success}
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

/**
  * a simulation command space used to describe command to modify simulation
  */
trait SimulationCommandSpace extends CommandSpace[CommandDescription] {
  /**
    * @return the simulation of this oommand space
    */
  def simulation : ExternalSimulation[_ <: AggregateWorld]

  /**
    * stop current simulation
    */
  case object StopSimulation extends Command {

    override def make(): CommandResult = {
      try {
        simulation.stop()
        Success
      } catch {
        case _ => Fail(illegalOrder)
      }


    }


    override def unmake(): CommandResult = {
      try {
        simulation.continue()
        Success
      } catch {
        case _ => Fail(illegalOrder)
      }
    }
  }

  /**
    * continue a simulation stopped
    */
  case object ContinueSimulation extends Command {

    override def make(): CommandResult = {
      try {
        simulation.continue()
        Success
      } catch {
        case _ => Fail(illegalOrder)
      }

    }

    override def unmake(): CommandResult = {
      try {
        simulation.stop()
        Success
      } catch {
        //TODO
        case _ => Fail(illegalOrder)
      }
    }
  }

  val descriptors : List[CommandDescription] = List(
    new CommandDescription("use stop to stop simulation","Stop command stop current simulation") {
      override def parseFromString(command: String): Option[Command] = if(command == "stop") Some(StopSimulation) else None
    },
    new CommandDescription("use continue to restart simulation stopped", "Continue command continue a simulation stopped") {
      override def parseFromString(command: String): Option[Command] = if(command == "continue") Some(ContinueSimulation) else None
    }
  )
}

