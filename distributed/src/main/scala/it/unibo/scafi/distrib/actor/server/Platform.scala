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

package it.unibo.scafi.distrib.actor.server

import it.unibo.scafi.distrib.actor.{Platform => BasePlatform}
import it.unibo.scafi.platform.Platform.PlatformDependency

/**
 * Specializes an [[it.unibo.scafi.distrib.actor.Platform]] into a "centralized platform" where
 *   - There is a central component in the system to which all the devices
 *     have to register and communicate in order to get info such as
 *     neighborhood state.
 */

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformServer
  with PlatformDevices { self: PlatformDependency =>

  class SettingsFactoryServer extends SettingsFactory {
    override def defaultProfileSettings(): ProfileSettings = ServerBasedActorSystemSettings()
  }

  @transient override val settingsFactory = new SettingsFactoryServer

  @transient override val platformFactory = new DistributedPlatformFactory {
    override def buildPlatformConfigurator(): PlatformConfigurator =
      PlatformConfigurator
  }

  /*********************************/
  /******** CMD-LINE PARSER ********/
  /*********************************/

  override def extendParser(p: scopt.OptionParser[Settings]) = {
    p.head("<scafi distributed system>", "1.0")

    p.opt[String]('H', "serverhost") valueName ("<SERVER_HOST>") action { (x, c) =>
      c.copy(profile = c.profile.copy(serverHost = x))
    } text ("Host of deployment of the subsystem node")

    p.opt[Int]('P', "serverport") valueName ("<SERVER_PORT>") action { (x, c) =>
      c.copy(profile = c.profile.copy(serverPort = x))
    } text ("Port of deployment of the subsystem node")

    p.opt[Unit]('g', "gui") action { (x, c) =>
      c.copy(profile = c.profile.copy(deviceGui = true))
    } text ("Device GUI")

    p.opt[Unit]('G', "servergui") action { (x, c) =>
      c.copy(profile = c.profile.copy(serverGui = true))
    } text ("SERVER GUI")

      /* COMMANDS */
    p.opt[Unit]("serverstart") action { (_, c) =>
      c.copy(profile = c.profile.copy(startServer = true))
    } text ("Starts the server")

    //note("some notes.\n")
    p.help("help") text ("Prints this usage text")
  }
}

object Platform {
  type Subcomponent = Platform with PlatformDependency
}
