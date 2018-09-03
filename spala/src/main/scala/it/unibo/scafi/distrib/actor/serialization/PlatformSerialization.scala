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
  val MsgExportManifest = "MsgExport"
  val MsgNeighborhoodExportsManifest = "MsgNeighborhoodExports"
  val MsgRegistrationManifest = "MsgRegistration"
  val MsgSensorValueManifest = "MsgSensorValue"

  override def manifest(obj: AnyRef): Option[String] = obj match {
    case _: MsgExport => Some(MsgExportManifest)
    case _: MsgNeighborhoodExports => Some(MsgNeighborhoodExportsManifest)
    case _: MsgRegistration => Some(MsgRegistrationManifest)
    case _: MsgSensorValue => Some(MsgSensorValueManifest)
    case _ => None
  }

  override def toBinary(obj: AnyRef): Option[Array[Byte]] = obj match {
    case me: MsgExport => Some(Json.toJson(me.asInstanceOf[MsgExport]).toString.getBytes)
    case mne: MsgNeighborhoodExports => Some(Json.toJson(mne.asInstanceOf[MsgNeighborhoodExports]).toString.getBytes)
    case mr: MsgRegistration => Some(Json.toJson(mr.asInstanceOf[MsgRegistration]).toString.getBytes)
    case msv: MsgSensorValue => Some(Json.toJson(msv.asInstanceOf[MsgSensorValue]).toString.getBytes)
    case _ => None
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): Option[AnyRef] = manifest match {
    case MsgExportManifest => Json.parse(bytes).validate[MsgExport] match {
      case s: JsSuccess[MsgExport] => Some(s.value)
      case _ => None
    }
    case MsgNeighborhoodExportsManifest => Json.parse(bytes).validate[MsgNeighborhoodExports] match {
      case s: JsSuccess[MsgNeighborhoodExports] => Some(s.value)
      case _ => None
    }
    case MsgRegistrationManifest => Json.parse(bytes).validate[MsgRegistration] match {
      case s: JsSuccess[MsgRegistration] => Some(s.value)
      case _ => None
    }
    case MsgSensorValueManifest => Json.parse(bytes).validate[MsgSensorValue] match {
      case s: JsSuccess[MsgSensorValue] => Some(s.value)
      case _ => None
    }
  }
}

trait JsonMessagesSerialization extends JsonOptionSerialization with JsonCollectionsSerialization { self: Platform =>
  implicit val msgExportWrites: Writes[MsgExport] = msg => Json.obj("from" -> anyToJs(msg.from), "export" -> anyToJs(msg.export))
  implicit val msgExportReads: Reads[MsgExport] = js =>
    JsSuccess { MsgExport(jsToAny((js \ "from").get).asInstanceOf[UID], jsToAny((js \ "export").get).asInstanceOf[ComputationExport]) }

  implicit val msgNeighborhoodExportsWrites: Writes[MsgNeighborhoodExports] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "nbrs" -> anyToJs(msg.nbrs))
  implicit val msgNeighborhoodExportsReads: Reads[MsgNeighborhoodExports] = js => JsSuccess { MsgNeighborhoodExports(jsToAny((js \ "id").get).asInstanceOf[UID],
    jsToAny((js \ "nbrs").get).asInstanceOf[Map[UID, Option[ComputationExport]]]) }

  implicit val msgRegistrationWrites: Writes[MsgRegistration] = msg => Json.obj("id" -> anyToJs(msg.id))
  implicit val msgRegistrationReads: Reads[MsgRegistration] = js => JsSuccess { MsgRegistration(jsToAny((js \ "id").get).asInstanceOf[UID]) }

  implicit val msgSensorValueWrites: Writes[MsgSensorValue] = msg =>
    Json.obj("id" -> anyToJs(msg.id), "name" -> anyToJs(msg.name), "value" -> anyToJs(msg.value))
  implicit val msgSensorValueReads: Reads[MsgSensorValue] = js => JsSuccess { MsgSensorValue(jsToAny((js \ "id").get).asInstanceOf[UID],
    jsToAny((js \ "name").get).asInstanceOf[LSensorName], jsToAny((js \ "value").get)) }

  override def anyToJs: PartialFunction[Any, JsValue] = super[JsonOptionSerialization].anyToJs orElse super[JsonCollectionsSerialization].anyToJs
}
