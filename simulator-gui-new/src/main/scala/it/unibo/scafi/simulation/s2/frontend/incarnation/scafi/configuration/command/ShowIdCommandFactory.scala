package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command.reverseCommand
import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Success

/**
  * allow to show the id of node in view form
  */
object ShowIdCommandFactory extends CommandFactory {
  import CommandFactory._
  private val deviceName = "id"

  private def toggle() = {
    for(node <- scafiWorld.nodes) {
      node.getDevice(deviceName) match {
        case Some(_) => scafiWorld.removeDevice(node.id,deviceName)
        case _ => scafiWorld.addDevice(node.id,scafiWorld.GeneralSensorProducer(deviceName,node.id,SensorConcept.sensorOutput))
      }
    }
    Success
  }
  override val name: String = "show-id"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] = List.empty

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = creationSuccessful(reverseCommand(toggle))
}
