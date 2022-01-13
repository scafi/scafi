package it.unibo.scafi.simulation.s2.frontend.configuration.command.factory

import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail
import it.unibo.scafi.simulation.s2.frontend.view.GraphicsLogger
import it.unibo.scafi.simulation.s2.frontend.view.GraphicsLogger.LogType

/**
  * a factory that create command that could change graphics log configuration
 */
class GraphicsLogCommandFactory extends CommandFactory {
  import CommandFactory._
  import GraphicsLogCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._

  private val logTypeMap : Map[String,LogType] = GraphicsLogger.logTypes.map{ x => x.toString -> x}.toMap
  private val typesAccept = LimitedValueType(logTypeMap.keySet)
  override val name: String = "graphics-log"

  override def commandArgsDescription: Seq[CommandArgDescription] =
    List(CommandArgDescription(Channel,typesAccept,description = international(name,Type)),
      CommandArgDescription(Type,StringType,description = international(name,Channel)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    var channel : Option[String] = None
    var valueType : Option[String] = None

    args.get(Type) match {
      case Some(typeValue : String) => if (logTypeMap.contains(typeValue)) {
        valueType = Some(typeValue)
      } else {
        creationFailed(Fail(wrongTypeParameter(typesAccept,Type)))
      }
    }

    args.get(Channel) match {
      case Some(channelValue : String) => channel = Some(channelValue)
      case _ => creationFailed(Fail(wrongTypeParameter(StringType,Channel)))
    }

    if(channel.isDefined && valueType.isDefined) {
      easyResultCreation(() => GraphicsLogger.addStrategy(channel.get,logTypeMap(valueType.get)))
    } else {
      creationFailed(Fail(wrongParameterName(Type,Channel)))
    }

  }
}

object GraphicsLogCommandFactory {
  val Type = "type"
  val Channel = "channel"
}