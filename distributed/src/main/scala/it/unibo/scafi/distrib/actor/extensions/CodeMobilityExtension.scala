package it.unibo.scafi.distrib.actor.extensions

import akka.actor._
import it.unibo.scafi.distrib.CustomClassLoader

/**
 * @author Roberto Casadei
 *
 */

class CodeMobilityExtensionImpl(val system: ExtendedActorSystem) extends Extension{
  def dynamicAccess: DynamicAccess = system.dynamicAccess
  def classloader: CustomClassLoader = dynamicAccess.classLoader.asInstanceOf[CustomClassLoader]
}

object CodeMobilityExtension extends ExtensionId[CodeMobilityExtensionImpl]
  with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem) =
    new CodeMobilityExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = CodeMobilityExtension
}
