package it.unibo.scafi.simulation.gui.configuration.command.factory

import it.unibo.scafi.simulation.gui.configuration.command.Command.reverseCommand
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * abstract command factory used to create a toggle sensor value factory
  * @param world the world where command is executed
  */
abstract class AbstractToggleCommandFactory(val world : SensorPlatform) extends CommandFactory {
  /**
    * create a toggle command
    * @param name the sensor name
    * @param ids set of ids where the sensor value changed
    * @return the command created
    */
  protected def toggle(name : world.NAME, ids : Iterable[world.ID]): Command = {
    val action = () => {
      for(id <- ids) {
        val node = world(id)
        //check if all id passed is in the world
        if(node.isDefined) {
          //check that the node has attached the device
          val sens = node.get.getDevice(name)
          if(sens.isDefined) {
            //if the device is a led sensor, i can change its value
            sens.get.value match {
              case led : Boolean => world.changeSensorValue(id,name,!led)
              case _ =>
            }
          }
        }
      }
      Success
    }
    reverseCommand(action)
  }
}
object AbstractToggleCommandFactory {
  val Name = "name"
  val Ids = "ids"
  class MultiToggleCommandFactory(override val world : SensorPlatform)(implicit val analyzer: WorldTypeAnalyzer)
    extends AbstractToggleCommandFactory(world) {
    import CommandFactory._
    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
      Seq(CommandArgDescription(Name,AnyType),CommandArgDescription (Ids, MultiValue(AnyType)))

    override protected def createPolicy(args: CommandArg): (Result,Option[Command]) = {
      var sensorName : Option[Any] = None
      var value : Option[Iterable[_]] = None
      //verify if the name argument is set and the name value is accepted
      args.get(Name) match {
        case Some(name : String) => if(analyzer.acceptName(name)) sensorName = Some(name)
        case _ => return creationFailed(Fail(wrongTypeParameter(AnyType,Name)))
      }
      //verify if the ids argument is set and the ids value is accepted
      args.get(Ids) match {
        case Some(ids : Iterable[_]) => if(ids.forall(analyzer.acceptId(_))) value = Some(ids)
        case _ => return creationFailed(Fail(wrongTypeParameter(MultiValue(AnyType),Ids)))
      }
      //if the argument passed has another name the creation of command if failed
      if(sensorName.isEmpty || value.isEmpty) {
        creationFailed(Fail(CommandFactory.wrongParameterName(Name, Ids)))
      } else {
        //otherwise i can create the new command used to toggle the id selected
        val name = sensorName.get.asInstanceOf[world.NAME]
        val ids = value.get.map{x => x.asInstanceOf[world.ID]}
        creationSuccessful(toggle(name,ids))
      }

    }
    override val name: String = "multi-toggle"
  }
}
