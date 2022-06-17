package it.unibo.scafi.simulation.s2.frontend.configuration.command.factory

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command.command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail
import it.unibo.scafi.simulation.s2.frontend.util.Result.Success

/**
 * a factory used to create command to manage a simulation
 * @param simulation
 *   the simulation managed by command
 */
class SimulationCommandFactory(simulation: ExternalSimulation[_]) extends CommandFactory {
  import CommandFactory._
  import SimulationCommandFactory._
  private val deltaTick = 1
  override val name: String = "simulation-action"

  override def commandArgsDescription: Seq[CommandArgDescription] =
    CommandArgDescription(Action, LimitedValueType(Continue, Stop, Restart, Slow, Fast)) :: Nil

  override def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    args.get(Action) match {
      case Some(Stop) => creationSuccessful(command(stop)(continue))
      case Some(Continue) => creationSuccessful(command(continue)(stop))
      case Some(Restart) => easyResultCreation(() => simulation.restart())
      case Some(Slow) => easyResultCreation(() => simulation.increaseDelta(deltaTick))
      case Some(Fast) => easyResultCreation(() => simulation.decreaseDelta(deltaTick))
      case Some(_) => creationFailed(Fail(wrongTypeParameter(LimitedValueType(Continue, Stop, Restart), Action)))
      case _ => creationFailed(Fail(wrongParameterName(Action)))
    }
  }

  private def stop(): Result = {
    // noinspection DangerousCatchAll
    try {
      simulation.stop()
      Success
    } catch {
      case throwable: Throwable => Fail(SimulationCommandFactory.StopError)
    }
  }

  private def continue(): Result = {
    // noinspection DangerousCatchAll
    try {
      simulation.continue()
      Success
    } catch {
      case throwable: Throwable => Fail(SimulationCommandFactory.ContinueError)
    }
  }
}

object SimulationCommandFactory {
  val Action = "action"
  val Stop = "stop"
  val Continue = "continue"
  val Restart = "restart"
  val Slow = "slow"
  val Fast = "fast"
  private[SimulationCommandFactory] val StopError = "the simulation is already stopped"
  private[SimulationCommandFactory] val ContinueError = "the simulation is already on run"
}
