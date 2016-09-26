package it.unibo.scafi.distrib.actor

import akka.actor.ExtendedActorSystem
import akka.serialization.{JavaSerializer, Serializer}

/**
 * @author Roberto Casadei
 *
 */

class CustomSerializer(ext: ExtendedActorSystem) extends Serializer {
  val javaSerializer = new JavaSerializer(ext)

  override def identifier: Int = 999

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
        ext.log.debug(s"\nCannot deserialize (NoClassDefFound): ${exc.getMessage}"+
          s"\nMissing class: $missingClass")
        SystemMsgClassNotFound(missingClass)
      }
    }
  }

  override def toBinary(o: AnyRef): Array[Byte] = {
    //println(s"\n### SERIALIZING: ${o}\n")
    javaSerializer.toBinary(o)
  }
}
