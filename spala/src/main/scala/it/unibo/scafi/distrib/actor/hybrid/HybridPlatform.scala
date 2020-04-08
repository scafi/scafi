/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.hybrid

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

trait HybridPlatform extends BasePlatform
  with PlatformDevices
  with PlatformServer
  with PlatformAPIFacade
{
  class SettingsFactoryHybrid extends SettingsFactory {
    override def defaultProfileSettings(): ProfileSettings = HybridActorSystemSettings()
    override def defaultSettings(): Settings = {
      val s = super.defaultSettings()
      s.copy(execution = s.execution.copy(scope = DeviceDelegated(DelayedDeviceExecStrategy(Some(1.second), 1.second))))
    }
  }

  @transient override val settingsFactory = new SettingsFactoryHybrid

  @transient override val platformFactory: DistributedPlatformFactory = new DistributedPlatformFactory {
    override def buildPlatformConfigurator() = PlatformConfigurator
  }

  /*********************************/
  /******** CMD-LINE PARSER ********/
  /*********************************/

  override def extendParser(p: scopt.OptionParser[Settings]): Unit = {
    p.head("<scafi distributed system>", "1.0")

    p.opt[String]('H', "serverhost") valueName "<SERVER_HOST>" action { (x, c) =>
      c.copy(profile = c.profile.copy(serverHost = x))
    } text "Host of deployment of the subsystem node"

    p.opt[Int]('P', "serverport") valueName "<SERVER_PORT>" action { (x, c) =>
      c.copy(profile = c.profile.copy(serverPort = x))
    } text "Port of deployment of the subsystem node"

    p.opt[Unit]('g', "gui") action { (x, c) =>
      c.copy(profile = c.profile.copy(deviceGui = true))
    } text "Device GUI"

    p.opt[Unit]('G', "servergui") action { (x, c) =>
      c.copy(profile = c.profile.copy(serverGui = true))
    } text "SERVER GUI"

    /* COMMANDS */
    p.opt[Unit]("serverstart") action { (_, c) =>
      c.copy(profile = c.profile.copy(startServer = true))
    } text "Starts the server"

    //note("some notes.\n")
    p.help("help") text "Prints this usage text"
  }
}

object HybridPlatform {
  type Subcomponent = HybridPlatform
}

