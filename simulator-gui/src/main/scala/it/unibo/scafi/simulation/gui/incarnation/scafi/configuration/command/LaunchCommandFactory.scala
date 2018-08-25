package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * a factory used a command that launch the scafi simulation
  * @param scafiConfiguration the scafi configuration builder
  */
class LaunchCommandFactory(implicit val scafiConfiguration : ScafiConfigurationBuilder)extends CommandFactory{
  import LaunchCommandFactory._
  import CommandFactory._
  override val name: String = "launch"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = creationSuccessful(onlyMakeCommand(() => {
    val configuration = scafiConfiguration.create()
    //if create return none it mean that some argument aren't set
    if(configuration.isEmpty) {
      //return fail
      Fail(ArgumentProblem)
    } else {
      //othewhise launch the scafi configuration
      ScafiProgramBuilder(configuration.get).launch()
      Success
    }
  }))
}

object LaunchCommandFactory {
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  implicit val key = KeyFile.Error
  def ArgumentProblem = i"argument-problem"
}