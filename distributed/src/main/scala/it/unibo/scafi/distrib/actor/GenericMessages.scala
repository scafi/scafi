package it.unibo.scafi.distrib.actor

import akka.actor.{Props, ActorRef}

/**
 * @author Roberto Casadei
 *
 * Here we define *general* message types.
 * Essentially to provide strongly-typed containers and
 *  linguistic abstractions for messages.
 */

// SCAFI MESSAGES MARKER TRAIT

/* It must extend "Serializable"
 * See here: https://groups.google.com/forum/embed/#!topic/akka-user/fhC8tv8fjlE
 */
trait ScafiMessage extends Serializable

// GENERIC

case class MsgWithInput[K,V](name: K, value: V)
case class MsgPropagate(msg: Any)

// ACTOR LIFECYCLE

object GoOn extends Serializable
object MsgStart extends Serializable
object MsgPause extends Serializable
object MsgStop extends Serializable
object MsgResume extends Serializable
object MsgShutdown extends Serializable

// OBSERVER PATTERN

case class MsgAddObserver(o: ActorRef)
case class MsgRemoveObserver(o: ActorRef)

// LOW-LEVEL

case class MsgAck(corr: Option[Any])
case class MsgCreateActor(props: Props, name: Option[String] = None, corr: Option[Any] = None)
case class MsgCreationAck(ref: ActorRef, name: String, corr: Option[Any] = None)

case class SystemMsgClassNotFound(className: String)
case class MsgRequestClass(className: String)
case class MsgWithClass(name: String, code:Array[Byte])
case class MsgWithClasses(classes: Map[String,Array[Byte]], corr: Option[Any])