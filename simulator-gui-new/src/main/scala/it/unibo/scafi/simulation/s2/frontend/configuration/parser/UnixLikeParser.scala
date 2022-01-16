package it.unibo.scafi.simulation.s2.frontend.configuration.parser

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory._
import it.unibo.scafi.simulation.s2.frontend.configuration.command.factory.ListCommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

import scala.util.Try

/**
 * a parser that parse string value with unix standard each command has -help instruction you can see all command with
 * list-command string
 * @param factories
 *   the command factory used to create commands
 */
class UnixLikeParser(factories: CommandFactory*) extends Parser[String] {
  import UnixLikeParser._
  // add list command factory used to list the command supported
  private val commandFactories: Seq[CommandFactory] = factories.+:(new ListCommandFactory(factories: _*))
  // the index in the string split of command name
  private val commandIndex = 0
  override def parse(arg: String): (Result, Option[Command]) = {
    // split the string with spaces
    val arrayArg = arg.split(" ")
    commandFactories.find(_.name == arrayArg(commandIndex)) match {
      // verify if in string contains -help
      case Some(factory) =>
        if (arrayArg.contains(Help)) {
          import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager._
          // create a command to show help associated to a command
          easyResultCreation(() => LogManager.notify(StringLog(Channel.CommandResult, Label.Empty, help(factory))))
        } else {
          // otherwise create a command by the right command factory, remove the first value that
          // match with command name
          factory.create(toCommandArg(factory, arrayArg.drop(1)))
        }
      // if the factory isn't present, return a fail value
      case None => (UnixLikeParser.NoCommandFound, None)
    }
  }

  private def toCommandArg(factory: CommandFactory, args: Array[String]): Map[String, Any] = {
    if (args.length > factory.commandArgsDescription.length) return Map.empty
    // create command arg via string
    args.indices.map { i =>
      factory.commandArgsDescription(i).name -> stringToValue(args(i), factory.commandArgsDescription(i))
    }
      .filter(x => x._2.isDefined)
      .map(x => x._1 -> x._2.get)
      .toMap
  }

  private def stringToValue(value: String, commandArgDescription: CommandArgDescription): Option[Any] = {
    // if value passed is equals to emptyValue return none
    if (value == emptyValue) return None
    // try to parse string value to the right value
    // some value aren't supported
    commandArgDescription.valueType match {
      case IntType =>
        if (Try(value.toLong).isSuccess) {
          Some(value.toInt)
        } else {
          None
        }
      case DoubleType =>
        if (Try(value.toDouble).isSuccess) {
          Some(value.toDouble)
        } else {
          None
        }
      case BooleanType =>
        if (Try(value.toBoolean).isSuccess) {
          Some(value.toBoolean)
        } else {
          None
        }
      case StringType => Some(value)
      case LimitedValueType(_*) => Some(value)
      case _ => None
    }
  }

  private def help(factory: CommandFactory): String = {
    implicit val file: String = KeyFile.Configuration
    val argument = i"argument"
    val description = i"description"
    val typeName = i"type"
    val optional = i"optional"
    val result = factory.name + " " + factory.description
    if (factory.commandArgsDescription.isEmpty) {
      result
    } else {
      result + s" $argument = " + factory.commandArgsDescription
        .map(x =>
          "[" + international(factory.name, x.name)(KeyFile.CommandName) + "] " +
            s"$description :" + x.description + "" +
            s"; $typeName = [" + x.valueType + "]" +
            (if (x.optional) s" [$optional]" else "") +
            (if (x.defaultValue.isDefined) " default = " + x.defaultValue.get.toString else "")
        )
        .mkString(";")
    }
  }
}

object UnixLikeParser {
  def NoCommandFound: Fail[String] = Fail(international("no-command-found")(KeyFile.Configuration))
  val Help = "-help"
  val emptyValue = "_"
}
