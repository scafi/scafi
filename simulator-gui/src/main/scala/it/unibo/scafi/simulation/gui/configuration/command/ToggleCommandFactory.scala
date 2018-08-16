package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.Command.reverseCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.ToggleCommandFactory.ToggleArg
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * toggle command factory used to create command used to on or off a sensor
  * @param world the world where the command exec
  * @param analyzer the type analyzer, to check the correctness of type at runtme
  */
class ToggleCommandFactory(private val world : SensorPlatform)(implicit val analyzer : WorldTypeAnalyzer) extends CommandFactory {
  override val name: CommandName = CommandFactory.CommandFactoryName.Toggle

  override def create(arg: CommandArg): Option[Command] = arg match {
    case ToggleArg(name,ids) => {
      if(!analyzer.acceptName(name)) None
      else if(!ids.forall(analyzer.acceptId(_))) None
      else Some(reverseCommand(() => toggle(name.asInstanceOf[world.NAME],ids.map{_.asInstanceOf[world.ID]})))
    }
    case _ => None
  }
  private def toggle(name : world.NAME, ids : Set[world.ID]): Result = {
    ids foreach ( x => {
      val node = world(x)
      if(node.isDefined && node.get.getDevice(name).isDefined) {
        node.get.getDevice(name).get.value match {
          case led : Boolean => world.changeSensorValue(node.get.id,name,!led)
          case _ =>
        }
      }
    })
    Success
  }
}

object ToggleCommandFactory {

  /**
    * a toggle argument used to create toggle command
    * @param name the sensor name
    * @param ids the id set that change the sensor value
    */
  case class ToggleArg(name : Any, ids : Set[Any]) extends CommandArg
}
