package demos

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.{ BasicActorServerBased => Platform }

/**
 * @author Roberto Casadei
 *
 */

object Server_MainProgram extends Platform.ServerCmdLineMain {
  override def refineSettings(s: Platform.Settings) = {
    s.copy(profile = s.profile.copy(
      serverGuiActorProps = tm => Some(ServerGUIActor.props(Platform, tm))
    ))
  }
}