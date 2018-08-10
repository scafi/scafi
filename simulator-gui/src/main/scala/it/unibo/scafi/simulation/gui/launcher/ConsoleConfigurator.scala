package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.command.CommandSpace
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command._
import it.unibo.scafi.simulation.gui.{demo => Demo}
/**
  * a command space used to configure a simulation
  */
abstract class ConsoleConfigurator extends CommandSpace[CommandDescription] {
  protected var node : Option[Int] = None
  protected var radius : Option[Int] = None
  protected var demo : Option[Class[_]] = None

  /**
    * @return true if the simulation was launched false otherwise
    */
  def launched : Boolean

  override def descriptors: List[Command.CommandDescription] = List(nodeDescription,simulationDescription,listDescription,radiusDescription)

  protected case class GeneralCommand(private val function : () => CommandResult) extends WithoutUndoCommand {
    override def make(): Command.CommandResult = function()
  }

  private val nodeDescription = new CommandDescription("use node=x to set to number of node in this simulation", "Node command allow to set the node in the simulation") {
    override def parseFromString(command: String): Option[Command] = {
      val regex = raw"node=(\d+)".r
      command match {
        case regex(number) => Some(GeneralCommand(() => {
          node = Some(number.toInt)
          Success
        }))
        case _ => None
      }
    }
  }

  private val simulationDescription = new CommandDescription("use simulation=x to set current simulation", "Simulation command allow to set the demo in the simulation") {
    override def parseFromString(command: String): Option[Command] = {
      val regex = raw"simulation=(.*)".r
      command match {
        case regex(sim) => Some(GeneralCommand(() => if(Demo.nameToDemoClass.contains(sim)){
            val simClass = Demo.nameToDemoClass(sim)
            demo = Some(simClass)
            Success
          } else {
            Fail("demo class don't exist")
          }))
        case _ => None
      }
    }
  }

  private val listDescription = new CommandDescription("use list demo to see al demo", "List demo allow to see all simulation class") {

    override def parseFromString(command: String): Option[Command] = if(command == "list demo") {
      Some(GeneralCommand(() =>  {
        Demo.demos.foreach(x => println(x.getSimpleName))
        Success
      }))
    } else {
      None
    }
  }

  private val radiusDescription = new CommandDescription("use radius=x to set the radius of current simulation", "Radius command allow to set the radius in this simulation") {

    override def parseFromString(command: String): Option[Command] = {
      val regex = raw"radius=(\d+)".r
      command match {
        case regex(r) => Some(GeneralCommand(() => {
          radius = Some(r.toInt)
          Success
        }))
        case _ => None
      }
    }
  }

}
