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

  implicit val computationExportWrites: Writes[ComputationExport]
  implicit val computationExportReads: Reads[ComputationExport]

  private def uidToJs(uid: UID): JsValue = uid match {
    case i: Int => Json.obj("id" -> i)
    case s: String => Json.obj("id" -> s)
  }
  private def jsToUid(js: JsValue): Any = (js \ "id").get match {
    case n: JsNumber => n.as[Int]
    case s: JsString => s.as[String]
  }

}

trait CustomSerializer extends SerializerWithStringManifest with Platform with PlatformJsonSerialization {
  private val Identifier = 4096
  private val MsgExportManifest = "MsgExport"; private val MsgNeighborhoodExportsManifest = "MsgNeighborhoodExports"

  val ext: ExtendedActorSystem

  override def identifier: Int = Identifier

  override def manifest(obj: AnyRef): String = obj match {
    case _: PlatformMessages#MsgExport => MsgExportManifest
    case _: PlatformMessages#MsgNeighborhoodExports => MsgNeighborhoodExportsManifest
  }

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case me: PlatformMessages#MsgExport => Json.toJson(me.asInstanceOf[MsgExport]).toString().getBytes
    case mne: PlatformMessages#MsgNeighborhoodExports => Json.toJson(mne.asInstanceOf[MsgNeighborhoodExports]).toString.getBytes
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case MsgExportManifest => Json.parse(bytes).validate[MsgExport] match {
      case s: JsSuccess[MsgExport] => s.value
      case e: JsError => ext.log.debug(s"\nCannot deserialize:" + JsError.toJson(e).toString)
        SystemMsgClassNotFound(JsError.toJson(e).toString)
    }
    case MsgNeighborhoodExportsManifest => Json.parse(bytes).validate[MsgNeighborhoodExports] match {
      case s: JsSuccess[MsgNeighborhoodExports] => s.value
      case e: JsError => ext.log.debug(s"\nCannot deserialize:" + JsError.toJson(e).toString)
        SystemMsgClassNotFound(JsError.toJson(e).toString)
    }
  }
}

/*
import akka.actor.ExtendedActorSystem
import akka.serialization.{JavaSerializer, Serializer}

class CustomSerializer(ext: ExtendedActorSystem) extends Serializer {
  val javaSerializer = new JavaSerializer(ext)

  override val identifier: Int = 999

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    try {
      javaSerializer.fromBinary(bytes)
    } catch {
      /**
       * Typically ClassNotFoundException means the current class is not found
       * and NoClassDefFoundError means a dependent class for the currently loaded class is not found
       * http://stackoverflow.com/questions/1457863/what-causes-and-what-are-the-differences-between-noclassdeffounderror-and-classn
       */
      case exc: ClassNotFoundException => {
        ext.log.debug(s"\nCannot deserialize (ClassNotFound): ${exc.getMessage}")
        //throw exc
        SystemMsgClassNotFound(exc.getMessage)
      }
      case exc: NoClassDefFoundError => {
        val missingClass = exc.getMessage.drop(1).dropRight(1).replace('/','.')
        ext.log.debug(s"\nCannot deserialize (NoClassDefFound): ${exc.getMessage}" +
          s"\nMissing class: $missingClass")
        SystemMsgClassNotFound(missingClass)
      }
    }
  }

  override def toBinary(o: AnyRef): Array[Byte] = {
    //ext.log.debug(s"\n### SERIALIZING: ${o}\n")
    javaSerializer.toBinary(o)
  }
}*/
