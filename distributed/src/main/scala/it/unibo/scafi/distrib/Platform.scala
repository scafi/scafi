package it.unibo.scafi.distrib

import it.unibo.scafi.platform.{Platform => BasePlatform}

/**
 * @author Roberto Casadei
 * This component defines a distributed platform and in particular:
 *   - A Façade API for the configuration, setup, and execution of distributed systems
 *   - A corpus of settings and defaults for distributed aggregate systems
 *   - Some general, utility members (string representations for types such as ID, LSNS,..),
 *     used for example in the command-line parser.
 */

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformSettings { self: BasePlatform.PlatformDependency =>

  override type EXPORT <: Export with ExportOps with Serializable
}

object Platform {
  type Subcomponent = Platform with BasePlatform.PlatformDependency
}