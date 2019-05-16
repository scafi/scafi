/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.distrib.actor.serialization

import it.unibo.scafi.distrib.actor.Platform
import play.api.libs.json._

trait BaseSerializer {
  def manifest(obj: AnyRef): Option[String]
  def toBinary(obj: AnyRef): Option[Array[Byte]]
  def fromBinary(bytes: Array[Byte], manifest: String): Option[AnyRef]
}

trait AbstractJsonPlatformSerializer extends BaseSerializer with JsonMessagesSerialization { self: Platform =>
  import AbstractJsonPlatformSerializer._

  override def manifest(obj: AnyRef): Option[String] = obj match {
    case _: MsgLocalSensorValue[_] => Some(MsgLocalSensorValueManifest)
    case _: MsgSensorValue[_] => Some(MsgSensorValueManifest)
    case _: MsgNbrSensorValue[_] => Some(MsgNbrSensorValueManifest)
    case _: MsgExport => Some(MsgExportManifest)
    case _: MsgExports => Some(MsgExportsManifest)
    case _: MsgNeighbor => Some(MsgNeighborManifest)
    case _: MsgNeighborhood => Some(MsgNeighborhoodManifest)
    case _: MyNameIs => Some(MyNameIsManifest)
    case _: MsgRound => Some(MsgRoundManifest)
    case _: MsgUpdateProgram => Some(MsgUpdateProgramManifest)
    case _: MsgPosition => Some(MsgPositionManifest)
    case _: MsgRegistration => Some(MsgRegistrationManifest)
    case _: MsgGetNeighborhoodLocations => Some(MsgGetNeighborhoodLocationsManifest)
    case _: MsgNeighborhoodLocations => Some(MsgNeighborhoodLocationsManifest)
    case _: MsgGetNeighborhoodExports => Some(MsgGetNeighborhoodExportsManifest)
    case _: MsgNeighborhoodExports => Some(MsgNeighborhoodExportsManifest)
    case _ => None
  }

  override def toBinary(obj: AnyRef): Option[Array[Byte]] = obj match {
    case ls: MsgLocalSensorValue[String] => Some(Json.toJson(ls).toString.getBytes)
    case sv: MsgSensorValue[String] => Some(Json.toJson(sv).toString.getBytes)
    case ns: MsgNbrSensorValue[String] => Some(Json.toJson(ns).toString.getBytes)
    case ex: MsgExport => Some(Json.toJson(ex).toString.getBytes)
    case es: MsgExports => Some(Json.toJson(es).toString.getBytes)
    case ng: MsgNeighbor => Some(Json.toJson(ng).toString.getBytes)
    case nh: MsgNeighborhood => Some(Json.toJson(nh).toString.getBytes)
    case mn: MyNameIs => Some(Json.toJson(mn).toString.getBytes)
    case rn: MsgRound => Some(Json.toJson(rn).toString.getBytes)
    case up: MsgUpdateProgram => Some(Json.toJson(up).toString.getBytes)
    case ps: MsgPosition => Some(Json.toJson(ps).toString.getBytes)
    case rg: MsgRegistration => Some(Json.toJson(rg).toString.getBytes)
    case gl: MsgGetNeighborhoodLocations => Some(Json.toJson(gl).toString.getBytes)
    case nl: MsgNeighborhoodLocations => Some(Json.toJson(nl).toString.getBytes)
    case ge: MsgGetNeighborhoodExports => Some(Json.toJson(ge).toString.getBytes)
    case ne: MsgNeighborhoodExports => Some(Json.toJson(ne).toString.getBytes)
    case _ => None
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): Option[AnyRef] = manifest match {
    case MsgLocalSensorValueManifest => Json.parse(bytes).validate[MsgLocalSensorValue[String]].asOpt
    case MsgSensorValueManifest => Json.parse(bytes).validate[MsgSensorValue[String]].asOpt
    case MsgNbrSensorValueManifest => Json.parse(bytes).validate[MsgNbrSensorValue[String]].asOpt
    case MsgExportManifest => Json.parse(bytes).validate[MsgExport].asOpt
    case MsgExportsManifest => Json.parse(bytes).validate[MsgExports].asOpt
    case MsgNeighborManifest => Json.parse(bytes).validate[MsgNeighbor].asOpt
    case MsgNeighborhoodManifest => Json.parse(bytes).validate[MsgNeighborhood].asOpt
    case MyNameIsManifest => Json.parse(bytes).validate[MyNameIs].asOpt
    case MsgRoundManifest => Json.parse(bytes).validate[MsgRound].asOpt
    case MsgUpdateProgramManifest => Json.parse(bytes).validate[MsgUpdateProgram].asOpt
    case MsgPositionManifest => Json.parse(bytes).validate[MsgPosition].asOpt
    case MsgRegistrationManifest => Json.parse(bytes).validate[MsgRegistration].asOpt
    case MsgGetNeighborhoodLocationsManifest => Json.parse(bytes).validate[MsgGetNeighborhoodLocations].asOpt
    case MsgNeighborhoodLocationsManifest => Json.parse(bytes).validate[MsgNeighborhoodLocations].asOpt
    case MsgGetNeighborhoodExportsManifest => Json.parse(bytes).validate[MsgGetNeighborhoodExports].asOpt
    case MsgNeighborhoodExportsManifest => Json.parse(bytes).validate[MsgNeighborhoodExports].asOpt
    case _ => None
  }
}

object AbstractJsonPlatformSerializer {

  // Input/information messages
  val MsgLocalSensorValueManifest = "MsgLocalSensorValue" // supported
  val MsgSensorValueManifest = "MsgSensorValue" //supported
  val MsgNbrSensorValueManifest = "MsgNbrSensorValue" //supported
  val MsgExportManifest = "MsgExport" // supported
  val MsgExportsManifest = "MsgExports" // supported
  val MsgDeviceLocationManifest = "MsgDeviceLocationManifest" // NOT supported
  val MsgWithDevicesManifest = "MsgWithDevices" // NOT supported
  val MsgNeighborManifest = "MsgNeighbor" // supported
  val MsgNeighborhoodManifest = "MsgNeighborhood" // supported
  val MsgMyFrequencyManifest = "MsgMyFrequency" // NOT supported
  val MyNameIsManifest = "MyNameIs" // supported
  val MsgRoundManifest = "MsgRound" // supported
  val MsgProgramManifest = "MsgProgram" // NOT supported
  val MsgAddSensorManifest = "MsgAddSensor" // NOT supported
  val MsgAddPushSensor = "MsgAddPushSensor" // NOT supported
  val MsgAddActuator = "MsgAddActuator" // NOT supported
  val DevInfoManifest = "DevInfo" // NOT supported
  val MsgUpdateProgramManifest = "MsgUpdateProgram" // supported
  val MsgPositionManifest = "MsgPosition" // supported

  // Invitation messages
  val MsgRegistrationManifest = "MsgRegistration" // supported
  val MsgSetFrequencyManifest = "MsgSetFrequency" // NOT supported
  val MsgRemoveNeighborManifest = "MsgRemoveNeighbor" // NOT supported
  val MsgShipProgramManifest = "MsgShipProgram" // NOT supported
  val MsgDeliverToManifest = "MsgDeliverTo" // NOT supported

  // Command messages
  val MsgAddDeviceManifest = "MsgAddDevice" // NOT supported

  // Request/Response messages
  val MsgGetNbrSensorValueManifest = "MsgGetNbrSensorValue" // NOT supported
  val MsgGetSensorValueManifest = "MsgGetSensorValue" // NOT supported
  val MsgLookupManifest = "MsgLookup" // NOT supported
  val MsgGetNeighborhoodManifest = "MsgGetNeighborhood" // NOT supported
  val MsgGetNeighborhoodLocationsManifest = "MsgGetNeighborhoodLocations" // supported
  val MsgNeighborhoodLocationsManifest = "MsgNeighborhoodLocations" // supported
  val MsgGetNeighborhoodExportsManifest = "MsgGetNeighborhoodExports" // supported
  val MsgNeighborhoodExportsManifest = "MsgNeighborhoodExports" // supported
  val AckManifest = "Ack" // NOT supported
}

object BasicSerializers {
  implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]]{
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) ⇒ implicitly[Writes[T]].writes(t)
      case None ⇒ JsNull
    }
  }

  implicit def mapFormat[K:Reads:Writes, V: Reads:Writes]: Format[Map[K,V]] = new Format[Map[K,V]] {
    override def writes(m: Map[K, V]): JsValue = ???

    override def reads(json: JsValue): JsResult[Map[K, V]] = ???
  }
}

trait JsonMessagesSerialization extends BasicJsonAnySerialization { self: Platform =>
  import play.api.libs.functional.syntax._ // for unlift etc.

  import BasicSerializers._
  import platformSerializer._

  implicit def msgLocalSensorValueWrites[T:Writes]: Writes[MsgLocalSensorValue[T]] = (
    (JsPath \ "name").write[LSensorName] and
      (JsPath \ "value").write[T]
  )(unlift(MsgLocalSensorValue.unapply[T]))

  implicit def msgLocalSensorValueReads[T:Reads]: Reads[MsgLocalSensorValue[T]] = (
      (JsPath \ "name").read[LSensorName] and
        (JsPath \ "value").read[T]
    )(MsgLocalSensorValue.apply[T] _)

  implicit def msgSensorValueWrites[T:Writes]: Writes[MsgSensorValue[T]] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "name").write[LSensorName] and
      (JsPath \ "value").write[T]
  )(unlift(MsgSensorValue.unapply[T]))

  implicit def msgSensorValueReads[T:Reads]: Reads[MsgSensorValue[T]] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "name").read[LSensorName] and
      (JsPath \ "value").read[T]
    )(MsgSensorValue.apply[T] _)

  implicit def msgNbrSensorValueWrites[T:Writes]: Writes[MsgNbrSensorValue[T]] = (
    (JsPath \ "name").write[NSensorName] and
      (JsPath \ "values").write[Map[UID,T]]
    )(unlift(MsgNbrSensorValue.unapply[T]))

  implicit def msgNbrSensorValueReads[T:Format]: Reads[MsgNbrSensorValue[T]] = (
    (JsPath \ "name").read[NSensorName] and
      (JsPath \ "values").read[Map[UID,T]]
    )(MsgNbrSensorValue.apply[T] _)

  implicit val msgExportWrites: Writes[MsgExport] = (
    (JsPath \ "from").write[UID] and
      (JsPath \ "export").write[ComputationExport]
    )(unlift(MsgExport.unapply))

  implicit val msgExportReads: Reads[MsgExport] = (
    (JsPath \ "from").read[UID] and
      (JsPath \ "export").read[ComputationExport]
    )(MsgExport.apply _)

  implicit val msgExportsWrites: Writes[MsgExports] =
    (JsPath \ "exports").write[Map[UID, ComputationExport]].contramap(msg => msg.exports)

  implicit val msgExportsReads: Reads[MsgExports] =
    (JsPath \ "exports").read[Map[UID, ComputationExport]].map(exports => MsgExports(exports))

  implicit val msgNeighborhoodExportsWrites: Writes[MsgNeighborhoodExports] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "nbrs").write[Map[UID, Option[ComputationExport]]]
    )(unlift(MsgNeighborhoodExports.unapply))

  implicit val msgNeighborhoodExportsReads: Reads[MsgNeighborhoodExports] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "nbrs").read[Map[UID, Option[ComputationExport]]]
    )(MsgNeighborhoodExports.apply _)

  implicit val msgRegistrationWrites: Writes[MsgRegistration] =
      (JsPath \ "id").write[UID].contramap(msg => msg.id)

  implicit val msgRegistrationReads: Reads[MsgRegistration] =
      (JsPath \ "id").read[UID].map(uid => MsgRegistration(uid))

  implicit val msgNeighborWrites: Writes[MsgNeighbor] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "idm").write[UID]
    )(unlift(MsgNeighbor.unapply))

  implicit val msgNeighborReads: Reads[MsgNeighbor] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "idn").read[UID]
    )(MsgNeighbor.apply _)

  implicit val msgNeighborhoodWrites: Writes[MsgNeighborhood] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "nbrs").write[Set[UID]]
    )(unlift(MsgNeighborhood.unapply))

  implicit val msgNeighborhoodReads: Reads[MsgNeighborhood] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "nbrs").read[Set[UID]]
    )(MsgNeighborhood.apply _)

  implicit val myNameIsWrites: Writes[MyNameIs] =
    (JsPath \ "id").write[UID].contramap(msg => msg.id)

  implicit val myNameIsReads: Reads[MyNameIs] =
    (JsPath \ "id").read[UID].map(uid => MyNameIs(uid))

  implicit val msgRoundWrites: Writes[MsgRound] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "n").write[Long]
    )(unlift(MsgRound.unapply))

  implicit val msgRoundReads: Reads[MsgRound] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "n").read[Long]
    )(MsgRound.apply _)

  implicit val msgGetNeighborhoodExportsWrites: Writes[MsgGetNeighborhoodExports] =
    (JsPath \ "id").write[UID].contramap(msg => msg.id)

  implicit val msgGetNeighborhoodExportsReads: Reads[MsgGetNeighborhoodExports] =
    (JsPath \ "id").read[UID].map(uid => MsgGetNeighborhoodExports(uid))

  implicit val msgUpdateProgramWrites: Writes[MsgUpdateProgram] = new Writes[MsgUpdateProgram] {
    override def writes(msg: MsgUpdateProgram): JsValue = Json.obj("id" -> anyToJs(msg.id), "program" -> anyToJs(msg.program))
  }

  implicit val msgUpdateProgramReads: Reads[MsgUpdateProgram] = new Reads[MsgUpdateProgram] {
    override def reads(js: JsValue) = JsSuccess(MsgUpdateProgram(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "program").get).asInstanceOf[()=>Any]))
  }

//  implicit val msgPositionWrites: Writes[MsgPosition] = (
//    (JsPath \ "id").write[UID] and
//      (JsPath \ "position").write[???]
//    )(unlift(MsgPosition.unapply))
//
//  implicit val msgPositionReads: Reads[MsgPosition] = (
//    (JsPath \ "id").read[UID] and
//      (JsPath \ "position").read[???]
//    )(MsgPosition.apply _)

  implicit val msgPositionWrites: Writes[MsgPosition] = new Writes[MsgPosition] {
    override def writes(msg: MsgPosition): JsValue = Json.obj("id" -> anyToJs(msg.id), "position" -> anyToJs(msg.position))
  }
  implicit val msgPositionReads: Reads[MsgPosition] = new Reads[MsgPosition] {
    override def reads(js: JsValue) = JsSuccess(MsgPosition(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "position").get)))
  }

  implicit val msgGetNeighborhoodLocationsWrites: Writes[MsgGetNeighborhoodLocations] =
    (JsPath \ "id").write[UID].contramap(msg => msg.id)

  implicit val msgGetNeighborhoodLocationsReads: Reads[MsgGetNeighborhoodLocations] =
    (JsPath \ "id").read[UID].map(uid => MsgGetNeighborhoodLocations(uid))

  implicit val msgNeighborhoodLocationsWrites: Writes[MsgNeighborhoodLocations] = (
    (JsPath \ "id").write[UID] and
      (JsPath \ "nbrs").write[Map[UID, String]]
    )(unlift(MsgNeighborhoodLocations.unapply))

  implicit val msgNeighborhoodLocationsReads: Reads[MsgNeighborhoodLocations] = (
    (JsPath \ "id").read[UID] and
      (JsPath \ "nbrs").read[Map[UID, String]]
    )(MsgNeighborhoodLocations.apply _)
}
