package it.unibo.scafi.simulation.gui.configuration.command.factory

import java.util.Locale

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

class LanguageCommandFactory extends CommandFactory {
  import CommandFactory._
  import LanguageCommandFactory._
  import ResourceBundleManager._
  private val map = Map("en" -> Locale.ENGLISH, "it" -> Locale.ITALIAN)
  private val supported = LimitedValueType(map.keySet.toSeq:_*)
  override def name: String = "language"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Name,supported,description = international(name,Name) ,defaultValue = Some("en")))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Name) match {
    case Some(nameValue : String) => if(map.contains(nameValue)) {
      easyResultCreation(() => ResourceBundleManager.locale = map(nameValue))
    } else {
      creationFailed(Fail(wrongTypeParameter(supported,Name)))
    }
    case Some(_) => creationFailed(Fail(wrongTypeParameter(supported,Name)))
    case _ => creationFailed(Fail(wrongParameterName(Name)))
  }
}

object LanguageCommandFactory {
  val Name = "name"
}