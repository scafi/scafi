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

package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.CustomSerializer
import it.unibo.scafi.distrib.actor.p2p.{Platform => P2pActorPlatform}
import it.unibo.scafi.distrib.actor.server.{Platform => ServerBasedActorPlatform}
import play.api.libs.json._

trait BasicActorIncarnationSerializer extends CustomSerializer with BasicAbstractActorIncarnation {
  override implicit val uidWrites: Writes[UID] = id => JsString(id.toString)
  override implicit val uidReads: Reads[UID] = id => JsSuccess { id.as[String].toInt }

  override implicit val computationExportWrites: Writes[ComputationExport] = export => {
    export.root().getClass.getName match {
      case "java.lang.Integer" => Json.obj("root" -> export.root().toString, "type" -> "Int")
      case _ => Json.obj("root" -> JsString(export.root()), "type" -> "String")
    }
  }
  override implicit val computationExportReads: Reads[ComputationExport] = js => {
    val export = new EngineFactory().emptyExport()
    (js \ "type").as[String] match {
      case "Int" => export.put(factory.emptyPath(), (js \ "root").as[Int]); JsSuccess { export }
      case _ => export.put(factory.emptyPath(), (js \ "root").as[String]); JsSuccess { export }
    }
  }
}

case class BasicActorP2pSerializer() extends BasicActorIncarnationSerializer with P2pActorPlatform
case class BasicActorServerBasedSerializer() extends BasicActorIncarnationSerializer with ServerBasedActorPlatform