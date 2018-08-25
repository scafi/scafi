package it.unibo.scafi.simulation.gui.controller.input

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.parser.{AnyParser, RuntimeMachine}
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * an input controller that execute command
  */
object InputCommandController extends InputController {
  type Argument = (CommandFactory,Map[String,Any])
  private var commands : List[Command] = List.empty
  private var undoList : List[Command] = List.empty
  private val maxSize = 10

  /**
    * put the command in the queue list
    * @param c
    */
  def exec(c : Command) = commands = commands ::: (c :: Nil)

  override def onTick(float: Float): Unit = {
    //for each command check that some of these are undo command
    commands foreach {x => {
      x match {
        case UndoCommand => undo() //if it is an undo, cancel last change
        case _ => addToUndoAndExec(x) //otherwise the input controller exec the command and add command to undo queue
      }
    }}
    commands = List.empty
  }

  private def addToUndoAndExec(c : Command ) {
    //exec the command
    c.make()
    //if the list has max size element
    undoList = if(undoList.size == maxSize) {
      //the new list hasn't the last child
      c :: undoList.dropRight(1)
    } else {
      //otherwise i add new command on head
      c :: undoList
    }
  }
  private def undo(): Unit = {
    //verify if the list has an element
    undoList = undoList.headOption match {
      case Some(head : Command) => {
        //unmake change
        head.unmake()
        //the new undo list is the last list without head
        undoList.tail
      }
      case _ => List.empty
    }
  }

  /**
    * a case object used to create a command mark as undo
    */
  case object UndoCommand extends Command {
    override val make: () => Result = () => Success
    override val unmake: () => Result = () => Success
  }

  /**
    * a factory that create an undo command
    */
  object UndoCommandFactory extends CommandFactory {
    override val name: String = "undo"

    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = List.empty

    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = CommandFactory.creationSuccessful(UndoCommand)
  }

  /**
    * a fast way to create command and add to command queue
    */
  val virtualMachine = new RuntimeMachine[Argument](new AnyParser())
}

