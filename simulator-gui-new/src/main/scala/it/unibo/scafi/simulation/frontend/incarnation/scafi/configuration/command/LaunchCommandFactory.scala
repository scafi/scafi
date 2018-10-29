package it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.frontend.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.util.Result.{Fail, Success}

/**
  * a factory used a command that launch the scafi simulation
  * @param scafiConfiguration the scafi configuration builder
  */
class LaunchCommandFactory(implicit val scafiConfiguration : ScafiConfigurationBuilder)extends CommandFactory{
  import CommandFactory._
  import LaunchCommandFactory._
  override val name: String = "launch"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = Seq.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = creationSuccessful(onlyMakeCommand(() => {
    val configuration = scafiConfiguration.create()
    //if create return none it mean that some argument aren't set
    if(configuration.isEmpty) {
      //return fail
      Fail(ArgumentProblem)
    } else {
      //otherwise launch the scafi configuration
      ScafiProgramBuilder(configuration.get).launch()
      Success
    }
  }))
}

object LaunchCommandFactory {
  import it.unibo.scafi.simulation.frontend.configuration.launguage.ResourceBundleManager._
  implicit val key : String = KeyFile.Error
  def ArgumentProblem = i"argument-problem"
}