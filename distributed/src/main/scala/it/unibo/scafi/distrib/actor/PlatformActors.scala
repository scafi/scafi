package it.unibo.scafi.distrib.actor

import akka.actor.{Props, Actor}

/**
 * @author Roberto Casadei
 *
 */

trait PlatformActors { self: Platform.Subcomponent =>

  /**
   * This is the top-level actor for a given aggregate application in the
   *  current subsystem.
   * Responsibilities
   *   - Creation of devices
   *   - Supervision of devices (as a consequence of previous point)
   * @param settings about the aggregate programming application
   */
  class AggregateApplicationActor(val settings: AggregateApplicationSettings) extends Actor {
    val logger = akka.event.Logging(context.system, this)

    def receive: Receive = {
      case MsgAddDevice(id, props) => {
        logger.info(s"\nCreating device $id")
        val dref = context.actorOf(props, deviceNameFromId(id))
        sender ! MsgDeviceLocation(id,dref)
      }
      case MsgCreateActor(props, nameOpt, corrOpt) => {
        val name = nameOpt.getOrElse("unnamed-" + System.currentTimeMillis())
        val aref = context.actorOf(props, name)
        sender ! MsgCreationAck(aref, name, corrOpt)
      }
      case MsgPropagate(msg) => {
        deviceActors.foreach(_ ! msg)
      }
      case MsgDeliverTo(id, msg) => {
        val recipient = context.children.find(_.path.name == deviceNameFromId(id))
        recipient.foreach(_ ! msg)
      }
      case MsgNeighbor(id,idn) => {
        val recipient = context.children.find(_.path.name == deviceNameFromId(id))
        val nbr = context.children.find(_.path.name == deviceNameFromId(idn))
        recipient.foreach(_ ! NbrInfo(idn,None,nbr,Some(self.path + "/" + deviceNameFromId(idn))))
      }
    }

    override def preStart() = {
      super.preStart()
      logger.info(s"\nSTARTED AGGREGATE APPLICATION '${settings.name}'")
    }

    val DEV_NAME_PREFIX = "dev-"

    private def deviceActors = context.children.filter(_.path.name.startsWith(DEV_NAME_PREFIX))

    private def deviceNameFromId(id: ID) = DEV_NAME_PREFIX+id
  }
  object AggregateApplicationActor extends Serializable {
    def props(as: AggregateApplicationSettings): Props =
      Props(classOf[AggregateApplicationActor], self, as)
  }

}
