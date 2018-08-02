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

package it.unibo.scafi.distrib.actor

import akka.actor.ExtendedActorSystem
import akka.serialization.SerializerWithStringManifest
import play.api.libs.json._

trait PlatformJsonSerialization { self: Platform.Subcomponent =>
  implicit val msgExportWrites: Writes[PlatformMessages#MsgExport] = Writes { msg =>
    Json.obj("from" -> uidToJs(msg.from.asInstanceOf[UID]), "export" -> Json.toJson(msg.export.asInstanceOf[ComputationExport]))
  }
  implicit val msgExportReads: Reads[MsgExport] = js =>
    JsSuccess { MsgExport(jsToUid((js \ "from").get).asInstanceOf[UID], (js \ "export").as[ComputationExport]) }

  implicit val msgNeighborhoodExportsWrites: Writes[PlatformMessages#MsgNeighborhoodExports] = Writes { msg =>
    Json.obj("id" -> uidToJs(msg.id.asInstanceOf[UID]), "nbrs" -> Json.toJson(msg.nbrs.asInstanceOf[Map[UID, Option[ComputationExport]]]))
  }
  implicit val msgNeighborhoodExportsReads: Reads[MsgNeighborhoodExports] = js =>
    JsSuccess { MsgNeighborhoodExports(jsToUid((js \ "id").get).asInstanceOf[UID], (js \ "nbrs").as[Map[UID, Option[ComputationExport]]]) }

  implicit val nbrsWrites: Writes[Map[UID, Option[ComputationExport]]] = map => Json.obj(map.map {
    case (k, v) =>
      (k.toString, Json.toJsFieldJsValueWrapper(v))
  }.toSeq: _*)
  implicit val nbrsReads: Reads[Map[UID, Option[ComputationExport]]] = js => JsSuccess {
    js.as[JsObject].value.map {
      case (k, v) => (jsToUid(Json.toJson(k)).asInstanceOf[UID], v.asOpt[ComputationExport])
    }.toMap
  }

  def uidToJs(uid: UID): JsValue
  def jsToUid(js: JsValue): Any

  implicit val computationExportWrites: Writes[ComputationExport]
  implicit val computationExportReads: Reads[ComputationExport]
}

trait BaseCustomSerializer extends PlatformJsonSerialization { self: Platform =>
  val MsgExportManifest = "MsgExport"
  val MsgNeighborhoodExportsManifest = "MsgNeighborhoodExports"

  def manifest(obj: AnyRef): Option[String] = obj match {
    case _: PlatformMessages#MsgExport => Some(MsgExportManifest)
    case _: PlatformMessages#MsgNeighborhoodExports => Some(MsgNeighborhoodExportsManifest)
    case _ => None
  }

  def toBinary(obj: AnyRef): Option[Array[Byte]] = obj match {
    case me: PlatformMessages#MsgExport =>
      Some(Json.toJson(me.asInstanceOf[MsgExport]).toString.getBytes)
    case mne: PlatformMessages#MsgNeighborhoodExports =>
      Some(Json.toJson(mne.asInstanceOf[MsgNeighborhoodExports]).toString.getBytes)
    case _ => None
  }

  def fromBinary(bytes: Array[Byte], manifest: String): Option[AnyRef] = manifest match {
    case MsgExportManifest => Json.parse(bytes).validate[MsgExport] match {
      case s: JsSuccess[MsgExport] => Some(s.value)
      case _ => None
    }
    case MsgNeighborhoodExportsManifest => Json.parse(bytes).validate[MsgNeighborhoodExports] match {
      case s: JsSuccess[MsgNeighborhoodExports] => Some(s.value)
      case _ => None
    }
  }
}

class CustomSerializer(ext: ExtendedActorSystem) extends SerializerWithStringManifest {
  private def incarnation = CustomSerializer.incarnation

  override def identifier: Int = 4096

  override def manifest(obj: AnyRef): String = incarnation.map(_.manifest(obj) match {
    case Some(m) => m
    case _ => obj.getClass.getName
  }).getOrElse("")

  override def toBinary(obj: AnyRef): Array[Byte] = incarnation.map(_.toBinary(obj) match {
    case Some(tb) => tb
    case _ => ext.log.debug(s"\nCannot serialize: " + obj); Array[Byte]()
  }).getOrElse(Array[Byte]())

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = incarnation.map(_.fromBinary(bytes, manifest) match {
    case Some(fb) => fb
    case _ => ext.log.debug(s"\nCannot deserialize: " + manifest); SystemMsgClassNotFound(manifest)
  }).getOrElse(None)
}

object CustomSerializer {
  var incarnation: Option[BaseCustomSerializer] = None
}
