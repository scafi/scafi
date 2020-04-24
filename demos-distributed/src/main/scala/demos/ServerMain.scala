/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.{ BasicActorServerBased => Platform }

object Server_MainProgram extends Platform.ServerCmdLineMain {
  override def refineSettings(s: Platform.Settings) = {
    s.copy(profile = s.profile.copy(
      serverGuiActorProps = tm => Some(ServerGUIActor.props(Platform, tm))
    ))
  }
}
