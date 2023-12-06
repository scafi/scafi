package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.MetaActionManager
import it.unibo.scafi.simulation.MetaActionManager.MetaAction
import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command.reverseCommand
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiBridge
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationExecutor
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationExecutor
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.s2.frontend.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail
import it.unibo.scafi.simulation.s2.frontend.util.Result.Success

/**
 * abstract command factory used to create a toggle sensor value factory
 */
abstract class AbstractToggleCommandFactory extends CommandFactory {
  /**
   * create a toggle command
   * @param name
   *   the sensor name
   * @param ids
   *   set of ids where the sensor value changed
   * @return
   *   the command created
   */
  protected def toggle(name: scafiWorld.NAME, ids: Iterable[ID]): Command = {
    val action = () => {
      var processAction = List.empty[MetaAction]
      val bridge = ScafiBridge.Instance.get.contract.simulation.get // scafiSimulationExecutor.contract.simulation.get
      for (id <- ids) {
        val node = scafiWorld(id)
        // check if all id passed is in the world
        if (node.isDefined) {
          // check that the node has attached the device
          val sens = node.get.getDevice(name)
          if (sens.isDefined) {
            // if the device is a led sensor, i can change its value
            sens.get.value[Any] match {
              case led: Boolean => processAction = bridge.NodeChangeSensor(id, name, !led) :: processAction
              case _ =>
            }
          }
        }
      }
      bridge.add(MetaActionManager.MultiAction(processAction: _*))
      Success
    }
    reverseCommand(action)
  }
}
object AbstractToggleCommandFactory {
  val Name = "name"
  val Ids = "ids"
  class MultiToggleCommandFactory extends AbstractToggleCommandFactory {
    import CommandFactory._
    override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
      Seq(CommandArgDescription(Name, AnyType), CommandArgDescription(Ids, MultiValue(AnyType)))

    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
      var sensorName: Option[Any] = None
      var value: Option[Iterable[_]] = None
      // verify if the name argument is set and the name value is accepted
      args.get(Name) match {
        case Some(name: String) => if (name.isInstanceOf[scafiWorld.NAME]) sensorName = Some(name)
        case _ => return creationFailed(Fail(wrongTypeParameter(AnyType, Name)))
      }
      // verify if the ids argument is set and the ids value is accepted
      args.get(Ids) match {
        case Some(ids: Iterable[_]) => if (ids.forall(_.isInstanceOf[scafiWorld.ID])) value = Some(ids)
        case _ => return creationFailed(Fail(wrongTypeParameter(MultiValue(AnyType), Ids)))
      }
      // if the argument passed has another name the creation of command if failed
      if (sensorName.isEmpty || value.isEmpty) {
        creationFailed(Fail(CommandFactory.wrongParameterName(Name, Ids)))
      } else {
        // otherwise i can create the new command used to toggle the id selected
        val name = sensorName.get.asInstanceOf[scafiWorld.NAME]
        val ids = value.get.map(x => x.asInstanceOf[scafiWorld.ID])
        creationSuccessful(toggle(name, ids))
      }

    }
    override val name: String = "multi-toggle"
  }
}
