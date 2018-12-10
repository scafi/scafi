package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Fail

/**
  * allow to change log configuration of scafi program
  * @param logConfiguration the log configuration accepted
  * @param scafiConfiguration the scafi configuration builder
  */
class LogCommandFactory (logConfiguration : LogConfiguration *)
                        (implicit val scafiConfiguration : ScafiConfigurationBuilder) extends CommandFactory {
  import CommandFactory._
  import LogCommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  private val logMapped = logConfiguration.map {x => x.toString -> x}.toMap
  private val typeAccepted = LimitedValueType(logMapped.keySet.toSeq:_*)

  override val name: String = "log-configuration"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(ConfigurationName,typeAccepted,
      description = international(name,ConfigurationName),defaultValue = Some(scafiConfiguration.logConfiguration.toString)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(ConfigurationName) match {
    case Some(name : String) if logMapped.contains(name) => easyResultCreation(() => scafiConfiguration.logConfiguration = logMapped(name))
    case Some(_) => creationFailed(Fail(wrongTypeParameter(typeAccepted,ConfigurationName)))
    case _ => creationFailed(Fail(wrongParameterName(ConfigurationName)))
  }
}

object LogCommandFactory {
  val ConfigurationName = "name"
}