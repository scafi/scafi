package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}
import it.unibo.scafi.simulation.gui.view.OutputPolicy

/**
  * a factory uses to create a command that set the output policy
  * @param outputs the outpolicy supported
  * @param scafiConfiguration implicit scafi configuration
  */
class OutputCommandFactory(outputs : OutputPolicy *)(implicit val scafiConfiguration : ScafiConfigurationBuilder) extends CommandFactory{
  import CommandFactory._
  import OutputCommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  private val outputMap = outputs map {x => x.toString -> x} toMap
  private val argType = LimitedValueType(outputMap.keySet.toSeq:_*)
  override val name: String = "output"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Name,
      argType,
      description = international(name, Name),
      defaultValue = scafiConfiguration.outputPolicy.map {_.toString}))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Name) match {
    case Some(value : String) => if(outputMap.get(value).isDefined) {
      easyResultCreation(() => scafiConfiguration.outputPolicy = outputMap.get(value))
    } else {
      creationFailed(Fail(wrongTypeParameter(argType,Name)))
    }
    case Some(_) =>  creationFailed(Fail(wrongTypeParameter(argType,Name)))
    case _ => creationFailed(Fail(wrongParameterName(Name)))
  }
}

object OutputCommandFactory {
  val Name = "name"
}