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

import akka.actor.ExtendedActorSystem
import akka.serialization.SerializerWithStringManifest
import it.unibo.scafi.distrib.actor.SystemMsgClassNotFound

class CustomAkkaSerializer(ext: ExtendedActorSystem) extends SerializerWithStringManifest {
  private def incarnationSerializer = CustomAkkaSerializer.incarnationSerializer

  override def identifier: Int = 4096

  override def manifest(obj: AnyRef): String = incarnationSerializer.flatMap(_.manifest(obj)).getOrElse(obj.getClass.getName)

  override def toBinary(obj: AnyRef): Array[Byte] = incarnationSerializer.map(_.toBinary(obj) match {
    case Some(tb) => tb
    case _ => ext.log.debug(s"\nCannot serialize: " + obj); Array[Byte]()
  }).getOrElse(Array[Byte]())

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = incarnationSerializer.map(_.fromBinary(bytes, manifest) match {
    case Some(fb) => fb
    case _ => ext.log.debug(s"\nCannot deserialize: " + manifest); SystemMsgClassNotFound(manifest)
  }).getOrElse(None)
}

object CustomAkkaSerializer {
  var incarnationSerializer: Option[BaseSerializer] = None
}
