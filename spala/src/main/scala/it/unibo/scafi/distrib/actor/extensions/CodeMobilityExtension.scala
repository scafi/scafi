/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.extensions

import akka.actor._
import it.unibo.scafi.distrib.CustomClassLoader

class CodeMobilityExtensionImpl(val system: ExtendedActorSystem) extends Extension{
  def dynamicAccess: DynamicAccess = system.dynamicAccess
  def classloader: CustomClassLoader = dynamicAccess.classLoader.asInstanceOf[CustomClassLoader]
}

object CodeMobilityExtension extends ExtensionId[CodeMobilityExtensionImpl]
  with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): CodeMobilityExtensionImpl =
    new CodeMobilityExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = CodeMobilityExtension
}
