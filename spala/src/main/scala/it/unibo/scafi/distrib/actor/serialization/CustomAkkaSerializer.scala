/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
    case Some(fb) => ext.log.debug("Deserialized into: "+fb); fb
    case _ => ext.log.debug(s"\nCannot deserialize: " + manifest); SystemMsgClassNotFound(manifest)
  }).getOrElse(None)
}

object CustomAkkaSerializer {
  var incarnationSerializer: Option[BaseSerializer] = None
}
