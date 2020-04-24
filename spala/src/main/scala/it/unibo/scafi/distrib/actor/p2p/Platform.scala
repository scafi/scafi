/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.p2p

import it.unibo.scafi.distrib.actor.{Platform => BasePlatform}
import scala.concurrent.duration.DurationInt

/**
 * Specializes an [[it.unibo.scafi.distrib.actor.Platform]] into a "decentralized platform" where
 *   - Devices interacts directly with one another
 * Issues to be solved / Decisions to be taken:
 *   - How to handle "platform sensors" (e.g., NBR_RANGE)?
 *   - How a device can become acquainted with another device?
 * It seems that, despite the architecture name, some centralization is needed.
 */

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformDevices {

  class SettingsFactoryP2P extends SettingsFactory{
    override def defaultProfileSettings(): ProfileSettings = P2PActorSystemSettings()
    override def defaultSettings(): Settings = {
      val s = super.defaultSettings()
      s.copy(execution = s.execution.copy(scope = DeviceDelegated(DelayedDeviceExecStrategy(Some(1.second), 1.second))))
    }
  }

  @transient override val settingsFactory = new SettingsFactoryP2P

  @transient override val platformFactory = new DistributedPlatformFactory {
    override def buildPlatformConfigurator(): PlatformConfigurator =
      PlatformConfigurator
  }

  /*********************************/
  /******** CMD-LINE PARSER ********/
  /*********************************/

  override def extendParser(p: scopt.OptionParser[Settings]): Unit = {
    p.head("** Scafi Decentralized Actor System Command Line Program **")

    p.opt[Boolean]('g', "gui") action { (x, c) =>
      c.copy(profile = P2PActorSystemSettings(deviceGui = x))
    } text ("Device GUI")
  }
}

object Platform {
  type Subcomponent = Platform
}
