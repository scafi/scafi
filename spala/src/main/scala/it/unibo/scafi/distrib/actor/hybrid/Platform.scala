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

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformServer
  with PlatformDevices {

  class SettingsFactoryHybrid extends SettingsFactory {
    override def defaultProfileSettings(): ProfileSettings = HybridActorSystemSettings()
    override def defaultSettings(): Settings = {
      val s = super.defaultSettings()
      s.copy(execution = s.execution.copy(scope = DeviceDelegated(DelayedDeviceExecStrategy(Some(1.second), 1.second))))
    }
  }

  @transient override val settingsFactory = new SettingsFactoryHybrid

  @transient override val platformFactory: DistributedPlatformFactory = () => PlatformConfigurator

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

object Platform {
  type Subcomponent = Platform
}

