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

import akka.serialization.SerializerWithStringManifest
import it.unibo.scafi.distrib.actor.ScafiMessage
import play.api.libs.json.{JsSuccess, Json}

class CustomSerializer extends SerializerWithStringManifest {
  private[this] val scafiMessageManifest = classOf[ScafiMessage].getName
  import CustomSerialization._

  override def identifier: Int = 1234
  override def manifest(obj: AnyRef): String = obj match {
    case _: ScafiMessage => scafiMessageManifest
    case _ => obj.getClass.getName
  }

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case scafiMsg: ScafiMessage => Json.toJson(scafiMsg).toString.getBytes
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case `scafiMessageManifest` => Json.parse(bytes).validate[ScafiMessage] match {
      case msg: JsSuccess[ScafiMessage] => msg.value
    }
  }
}
