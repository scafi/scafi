package it.unibo.scafi.simulation.gui.test.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, CommandArgDescription, IntType, StringType}
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.parser.UnixLikeParser
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}
import org.scalatest.{FunSpec, Matchers}

class UnixParserTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val commandName = "command"
  val commandNotDescribed = "acommand"
  val listCommand = "list-command"
  val help = "-help"
  val firstValue = 1
  val aValue = "avalue"
  val aFactory = new CommandFactory {
    override val description: String = ""

    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
      if(args.contains("int") && args.contains("string")) {
        CommandFactory.creationSuccessful(onlyMakeCommand(() => Success))
      } else {
        CommandFactory.creationFailed(Fail("fail"))
      }
    }

    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
      List(CommandArgDescription("int",IntType),CommandArgDescription("string",StringType))

    override val name: String = commandName
  }

  val parser = new UnixLikeParser(aFactory)

  checkThat("i can parser a command describe by a factory"){
    val result = parser.parse(s"$commandName $firstValue $aValue")
    result._1 shouldBe Success

  }

  checkThat("i can't create a command not described by factory") {
    val result = parser.parse(s"$commandNotDescribed $aValue $aValue")
    result._1 match {
      case Success => assert(false)
      case _=>
    }
  }

  checkThat("i can't create a command with wrong type value") {
    val result = parser.parse(s"$commandName $aValue $aValue")
    result._1 match {
      case Success => assert(false)
      case _ =>
    }
  }

  checkThat("i can't change the argument position") {
    val result = parser.parse(s"$commandName $aValue $firstValue")
    result._1 match {
      case Success => assert(false)
      case _ =>
    }
  }

  checkThat("i can list all command") {
    val result = parser.parse(s"$listCommand")
    result._1 shouldBe Success
  }

  checkThat("i can see help of a command") {
    val result = parser.parse(s"$commandName $help")
    result._1 shouldBe Success
  }
}
