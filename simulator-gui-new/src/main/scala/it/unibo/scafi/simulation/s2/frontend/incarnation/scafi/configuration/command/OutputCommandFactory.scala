package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail
import it.unibo.scafi.simulation.s2.frontend.view.OutputPolicy

/**
  * a factory uses to create a command that set the output policy
  * @param outputs the outpolicy supported
  * @param scafiConfiguration implicit scafi configuration
  */
class OutputCommandFactory(outputs : OutputPolicy *)(implicit val scafiConfiguration : ScafiConfigurationBuilder) extends CommandFactory{
  import CommandFactory._
  import OutputCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  private val outputMap = outputs.map{x => x.toString -> x}.toMap
  private val argType = LimitedValueType(outputMap.keySet.toSeq:_*)
  override val name: String = "output"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Name,
      argType,
      description = international(name, Name),
      defaultValue = Some(scafiConfiguration.outputPolicy.toString)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Name) match {
    case Some(value : String) => if(outputMap.get(value).isDefined) {
      easyResultCreation(() => scafiConfiguration.outputPolicy = outputMap(value))
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