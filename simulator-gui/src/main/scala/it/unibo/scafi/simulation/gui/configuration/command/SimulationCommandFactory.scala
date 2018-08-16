package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.command
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.SimulationCommandFactory.{ContinueArg, StopArg}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * a simulation command factory used to create command to manage a simulation
  * @param simulation the simulation managed
  */
class SimulationCommandFactory(simulation : ExternalSimulation[_ <: AggregateWorld]) extends CommandFactory{

  override val name: CommandName = CommandFactory.CommandFactoryName.Simulation

  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case StopArg => Some(command(stop)(continue))
    case ContinueArg => Some(command(continue)(stop))
    case _ => None
  }

  private def stop() : Result =  try {
    simulation.stop()
    Success
  } catch {
    case _ => Fail(SimulationCommandFactory.alreadyStopped)
  }

  private def continue() : Result = try {
    simulation.continue()
    Success
  } catch  {
    case _ => Fail(SimulationCommandFactory.notStopped)
  }
}

object SimulationCommandFactory {
  private [SimulationCommandFactory] val alreadyStopped = "the simulation is already stopped"

  private [SimulationCommandFactory] val notStopped = "the simulation isn't stopped "

  /**
    * stop arg used to stopped the simulation
    */
  object StopArg extends CommandArg {
    override def toString: String = "stop"
  }

  /**
    * continue arg used to continue the simulation
    */
  object ContinueArg extends CommandArg {
    override def toString: String = "continue"
  }

  object SimulationStringParser extends StringCommandParser {
    /**
      * try to create command arg via value passed
      *
      * @return None if the value is not legit Some of argument otherwise
      */
    override def parse: Option[CommandArg] = arg match {
      case "stop" => Some(StopArg)
      case "continue" => Some(ContinueArg)
      case _ => None
    }

    /**
      * describe a way to use parser
      *
      * @return a help
      */
    override def help: String = "use stop to stop current simulation\nuse continue to restart a simulation"
}
}
