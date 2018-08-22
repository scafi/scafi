package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.command
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, CommandArgDescription, LimitedValueType}
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * a factory used to create command to manage a simulation
  * @param simulation the simulation managed by command
  */
class SimulationCommandFactory(simulation : ExternalSimulation[_]) extends CommandFactory {
  import SimulationCommandFactory._
  import CommandFactory._
  override val name: String = "simulation-action"

  override def commandArgsDescription: Seq[CommandArgDescription] = CommandArgDescription(Action,
    LimitedValueType(Continue, Stop)) :: Nil

  override def createPolicy(args: CommandArg): (Result,Option[Command]) = {
    args.get(Action) match {
      case Some(Stop) => creationSuccessful(command(stop)(continue))
      case Some(Continue) => creationSuccessful(command(continue)(stop))
      case Some(_) => creationFailed(Fail(wrongTypeParameter(LimitedValueType(Continue,Stop),Action)))
      case _ => creationFailed(Fail(wrongParameterName(Action)))
    }
  }

  private def stop() : Result = {
    try {
      simulation.stop()
      Success
    } catch {
      case e => Fail(SimulationCommandFactory.StopError)
    }
  }

  private def continue() : Result = {
    try {
      simulation.continue()
      Success
    } catch {
      case e => Fail(SimulationCommandFactory.ContinueError)
    }
  }
}

object SimulationCommandFactory {
  val Action = "action"
  val Stop = "stop"
  val Continue = "continue"
  private[SimulationCommandFactory] val StopError = "the simulation is already stopped"
  private[SimulationCommandFactory] val ContinueError = "the simulation is already on run"
}
