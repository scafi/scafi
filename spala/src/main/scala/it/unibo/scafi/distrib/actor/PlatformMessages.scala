/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props}
import akka.remote.ContainerFormats
import it.unibo.scafi.space.Point2D
import javax.swing.JComponent

import scala.concurrent.duration.FiniteDuration

// scalastyle:off number.of.types number.of.methods
trait PlatformMessages { self: Platform.Subcomponent =>
  // Input/information messages (which provides data to the recipient actor)
  case class MsgLocalSensorValue[T](name: LSensorName, value: T)
  case class MsgSensorValue[T](id: UID, name: LSensorName, value: T)
  case class MsgNbrSensorValue[T](name: NSensorName, values: Map[UID,T])
  case class MsgExport(from: UID, export: ComputationExport) extends ScafiMessage
  case class MsgExports(exports: Map[UID, ComputationExport])
  case class MsgDeviceLocation(id: UID, ref: ActorRef)
  case class MsgWithDevices(devs: Map[UID, ActorRef])
  case class MsgNeighbor(id: UID, idn: UID)
  case class MsgNeighborhood(id: UID, nbrs: Set[UID])
  case class MsgMyFrequency(delay: FiniteDuration)
  case class MyNameIs(id: UID)
  case class MsgRound(id: UID, n: Long)
  case class MsgProgram(ap: ProgramContract, dependencies: Set[Class[_]] = Set())
  case class MsgAddSensor(name: LSensorName, provider: ()=>Any)
  case class MsgAddPushSensor(ref: ActorRef)
  case class MsgAddActuator(name: LSensorName, consumer: Any=>Unit)
  case class DevInfo(nid: UID, ref: ActorRef)
  case class MsgUpdateProgram(id: UID, program: () => Any)
  case class MsgPosition(id: UID, position: Any)

  // Invitation messages (please do "that" for me; the sender expects no reply)
  case class MsgRegistration(id: UID)
  case class MsgSetFrequency(n: Int, unit: TimeUnit)
  case class MsgRemoveNeighbor(idn: UID)
  case class MsgShipProgram(programMsg: MsgProgram)
  case class MsgDeliverTo(id: UID, msg: Any)

  // Command messages (please do "that" for me; the sender *does* expect some reply)
  case class MsgAddDevice(id: UID, props: Props)

  // Request/Response messages (the sender expects a reply from the recipient)
  case class MsgGetNbrSensorValue(sns: NSensorName, idn: UID)
  case class MsgGetSensorValue(sns: LSensorName)
  case class MsgLookup(id: UID)
  case class MsgGetNeighborhood(id: UID)
  case class MsgGetNeighborhoodLocations(id: UID)
  case class MsgNeighborhoodLocations(id: UID, nbrs: Map[UID, String])
  case class MsgGetNeighborhoodExports(id: UID)
  case class MsgNeighborhoodExports(id: UID, nbrs: Map[UID,Option[ComputationExport]]) extends ScafiMessage
  val MsgGetIds: Int = "msg_get_ids".hashCode
  val MsgGetExport: Int = "msg_get_export".hashCode
  val MsgGetNeighbors: Int = "msg_get_neighbors".hashCode
  case class Ack(id: UID)

  // View messages
  case class MsgDevsGUIActor(devsGuiActor: ActorRef)
  case class MsgAddDevComponent(ref: ActorRef, devComponent: JComponent)
  case class MsgDevName(ref: ActorRef, id: UID)
  case class MsgDevPosition(ref: ActorRef, pos: Point2D)
  case class MsgNeighborhoodUpdate(id: UID, nbrs: Map[UID, ActorRef])
}
