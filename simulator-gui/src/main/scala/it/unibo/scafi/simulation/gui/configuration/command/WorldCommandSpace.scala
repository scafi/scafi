package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.WorldCommandSpace.{WorldTypeAcceptor, wrongIdType, wrongNameType}
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command._
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * a command space used to describe command that modify the world
  * @tparam W the world type
  */
trait WorldCommandSpace[W <: SensorPlatform] extends CommandSpace [CommandDescription]{
  /**
    * the world modified by command
    */
  val world : W

  /**
    * the world acceptor type, used to verify the correctness of type in run time
    * @return
    */
  def acceptor : WorldTypeAcceptor
  /**
    * a command used to move a set of id to another position in a scafi world
    * @param ids the id to move
    */
  case class MoveCommand(private val ids : Map[_ <: Any, Point3D]) extends Command {
    var oldPos : Map[world.ID,world.P] = Map()
    override def make(): CommandResult = {
      if(! ids.forall(x => acceptor.acceptId(x._1))) return Fail(wrongIdType)
      ids.foreach(x => {
        val id = x._1.asInstanceOf[world.ID]
        oldPos += id -> world(id).get.position
        world.moveNode(id,x._2.asInstanceOf[world.P])
      })
      Success
    }

    override def unmake(): CommandResult = {
      oldPos foreach { x => world.moveNode(x._1,x._2)}
      Success
    }

  }

  /**
    * switch on or off a sensor in a scafi world
    * @param ids the set of node id
    * @param name the name of device
    */
  case class ToggleDeviceCommand(private val ids : Set[_ <: Any], private val name : Any) extends Command {
    private def toggleDevice(): CommandResult =  {
      if(!acceptor.acceptName(name)) return Fail(wrongNameType)
      if(!ids.forall{ acceptor.acceptId(_)}) return Fail(wrongIdType)
      val worldName = name.asInstanceOf[world.NAME]
      ids foreach (x => {
        val worldId = x.asInstanceOf[world.ID]
        val node = world(worldId)
        if(node.isDefined) {
          val dev = node.get.getDevice(worldName)

          dev.foreach{ x => x match {
            case SensorDevice(sens) => sens.value match {
              case led: Boolean => world.changeSensorValue(worldId, worldName, !led)
              case _ =>
            }
          }}
        }
      })
      Success
    }


    override def make(): CommandResult = toggleDevice()

    override def unmake(): CommandResult = toggleDevice()
  }

}

object WorldCommandSpace {

  /**
    * a strategy used to check the correctness of type
    */
  trait WorldTypeAcceptor {
    /**
      * verify if the id is correct
      * @param id the current id
      * @return true if is is correct false otherwise
      */
    def acceptId(id : Any) : Boolean

    /**
      * verify if the name is correct
      * @param name the name
      * @return true if is correct false otherwise
      */
    def acceptName(name : Any) : Boolean
  }

  /**
    * command fail because the id type is wrong
    */
  object wrongIdType extends FailReason {
    override def reason: String = "wrong id type passed"
  }

  /**
    * command fail because the name type is wrong
    */
  object wrongNameType extends FailReason {
    override def reason: String = "wrong name type passed"
  }
}