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
}
