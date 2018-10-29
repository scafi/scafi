package it.unibo.scafi.simulation.frontend.test.configuration

import it.unibo.scafi.simulation.frontend.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.frontend.configuration.command.CommandFactory.{CommandArg, CommandArgDescription, IntType, StringType}
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.configuration.parser.UnixLikeParser
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.util.Result.{Fail, Success}
import org.scalatest.{FunSpec, Matchers}

//noinspection NameBooleanParameters,NameBooleanParameters,NameBooleanParameters
class UnixParserTest extends FunSpec with Matchers {
  private val checkThat = new ItWord
  private val commandName = "command"
  private val commandNotDescribed = "a-command"
  private val listCommand = "list-command"
  private val help = "-help"
  private val firstValue = 1
  private val aValue = "a-value"
  private val aFactory = new CommandFactory {
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
