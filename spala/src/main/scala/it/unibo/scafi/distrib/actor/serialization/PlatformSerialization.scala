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
    case _: MsgSensorValue => Some(MsgSensorValueManifest)
    case _: MsgNbrSensorValue => Some(MsgNbrSensorValueManifest)
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
    case ls: MsgLocalSensorValue[_] => Some(Json.toJson(ls).toString.getBytes)
    case sv: MsgSensorValue => Some(Json.toJson(sv).toString.getBytes)
    case ns: MsgNbrSensorValue => Some(Json.toJson(ns).toString.getBytes)
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
    case MsgLocalSensorValueManifest => Json.parse(bytes).validate[MsgLocalSensorValue[_]].asOpt
    case MsgSensorValueManifest => Json.parse(bytes).validate[MsgSensorValue].asOpt
    case MsgNbrSensorValueManifest => Json.parse(bytes).validate[MsgNbrSensorValue].asOpt
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

trait JsonMessagesSerialization extends JsonBaseSerialization { self: Platform =>
  implicit val msgLocalSensorValueWrites: Writes[MsgLocalSensorValue[_]] = msg =>
    Json.obj("name" -> anyToJs(msg.name), "value" -> anyToJs(msg.value))
  implicit val msgLocalSensorValueReads: Reads[MsgLocalSensorValue[_]] = js => JsSuccess {
    MsgLocalSensorValue(jsToAny((js \ "name").get).asInstanceOf[LSensorName], jsToAny((js \ "value").get))
  }

  implicit val msgSensorValueWrites: Writes[MsgSensorValue] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "name" -> anyToJs(msg.name), "value" -> anyToJs(msg.value))
  implicit val msgSensorValueReads: Reads[MsgSensorValue] = js => JsSuccess {
    MsgSensorValue(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "name").get).asInstanceOf[LSensorName], jsToAny((js \ "value").get))
  }

  implicit val msgNbrSensorValueWrites: Writes[MsgNbrSensorValue] = msg =>
    Json.obj("name" -> anyToJs(msg.name), "values" -> anyToJs(msg.values))
  implicit val msgNbrSensorValueReads: Reads[MsgNbrSensorValue] = js => JsSuccess {
    MsgNbrSensorValue(jsToAny((js \ "name").get).asInstanceOf[NSensorName], jsToAny((js \ "values").get).asInstanceOf[Map[UID, Any]])
  }

  implicit val msgExportWrites: Writes[MsgExport] = msg =>
    Json.obj("from" -> anyToJs(msg.from), "export" -> anyToJs(msg.export))
  implicit val msgExportReads: Reads[MsgExport] = js => JsSuccess {
    MsgExport(jsToAny((js \ "from").get).asInstanceOf[UID], jsToAny((js \ "export").get).asInstanceOf[ComputationExport])
  }

  implicit val msgExportsWrites: Writes[MsgExports] = msg =>
    Json.obj("exports" -> anyToJs(msg.exports))
  implicit val msgExportsReads: Reads[MsgExports] = js => JsSuccess {
    MsgExports(jsToAny((js \ "exports").get).asInstanceOf[Map[UID, ComputationExport]])
  }

  implicit val msgNeighborhoodExportsWrites: Writes[MsgNeighborhoodExports] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "nbrs" -> anyToJs(msg.nbrs))
  implicit val msgNeighborhoodExportsReads: Reads[MsgNeighborhoodExports] = js => JsSuccess {
    MsgNeighborhoodExports(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "nbrs").get).asInstanceOf[Map[UID, Option[ComputationExport]]])
  }

  implicit val msgRegistrationWrites: Writes[MsgRegistration] = msg =>
    Json.obj("id" -> anyToJs(msg.id))
  implicit val msgRegistrationReads: Reads[MsgRegistration] = js => JsSuccess {
    MsgRegistration(jsToAny((js \ "id").get).asInstanceOf[UID])
  }

  implicit val msgNeighborWrites: Writes[MsgNeighbor] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "idn" -> anyToJs(msg.idn))
  implicit val msgNeighborReads: Reads[MsgNeighbor] = js => JsSuccess {
    MsgNeighbor(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "idn").get).asInstanceOf[UID])
  }

  implicit val msgNeighborhoodWrites: Writes[MsgNeighborhood] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "nbrs" -> anyToJs(msg.nbrs))
  implicit val msgNeighborhoodReads: Reads[MsgNeighborhood] = js => JsSuccess {
    MsgNeighborhood(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "nbrs").get).asInstanceOf[Set[UID]])
  }

  implicit val myNameIsWrites: Writes[MyNameIs] = msg =>
    Json.obj("id" -> anyToJs(msg.id))
  implicit val myNameIsReads: Reads[MyNameIs] = js => JsSuccess {
    MyNameIs(jsToAny((js \ "id").get).asInstanceOf[UID])
  }

  implicit val msgRoundWrites: Writes[MsgRound] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "n" -> anyToJs(msg.n))
  implicit val msgRoundReads: Reads[MsgRound] = js => JsSuccess {
    MsgRound(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "n").get).asInstanceOf[Long])
  }

  implicit val msgGetNeighborhoodExportsWrites: Writes[MsgGetNeighborhoodExports] = msg =>
    Json.obj("id" -> anyToJs(msg.id))
  implicit val msgGetNeighborhoodExportsReads: Reads[MsgGetNeighborhoodExports] = js => JsSuccess {
    MsgGetNeighborhoodExports(jsToAny((js \ "id").get).asInstanceOf[UID])
  }

  implicit val msgUpdateProgramWrites: Writes[MsgUpdateProgram] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "program" -> anyToJs(msg.program))
  implicit val msgUpdateProgramReads: Reads[MsgUpdateProgram] = js => JsSuccess {
    MsgUpdateProgram(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "program").get).asInstanceOf[()=>Any])
  }

  implicit val msgPositionWrites: Writes[MsgPosition] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "position" -> anyToJs(msg.position))
  implicit val msgPositionReads: Reads[MsgPosition] = js => JsSuccess {
    MsgPosition(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "position").get))
  }

  implicit val msgGetNeighborhoodLocationsWrites: Writes[MsgGetNeighborhoodLocations] = msg =>
    Json.obj("id" -> anyToJs(msg.id))
  implicit val msgGetNeighborhoodLocationsReads: Reads[MsgGetNeighborhoodLocations] = js => JsSuccess {
    MsgGetNeighborhoodLocations(jsToAny((js \ "id").get).asInstanceOf[UID])
  }

  implicit val msgNeighborhoodLocationsWrites: Writes[MsgNeighborhoodLocations] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "nbrs" -> anyToJs(msg.nbrs))
  implicit val msgNeighborhoodLocationsReads: Reads[MsgNeighborhoodLocations] = js => JsSuccess {
    MsgNeighborhoodLocations(jsToAny((js \ "id").get).asInstanceOf[UID], jsToAny((js \ "nbrs").get).asInstanceOf[Map[UID, String]])
  }
}
