/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

/**
 * Demo 0-A
 * - Peer-to-peer system
 * - Ad-hoc network
 * - Programmatic configuration
 */

import scala.concurrent.duration._

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.{ BasicActorP2P => Platform }

object Demo0A_Inputs {
  // STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
  trait Demo0A_AggregateProgram extends Platform.ScafiStandardAggregateProgram {
    override def main(): Any = foldhood(0){_ + _}(1)
  }

  // STEP 3: DEFINE SETTINGS (E.G., PROGRAMMATICALLY)
  val aggregateAppSettings = Platform.AggregateApplicationSettings(
    name = "demo0A",
    program = () => Some(new Demo0A_AggregateProgram {})
  )
  val deploymentSubsys1 = Platform.DeploymentSettings(host = "127.0.0.1", port = 9000)
  val deploymentSubsys2 = Platform.DeploymentSettings(host = "127.0.0.1", port = 9500)

  val settings1 = Platform.settingsFactory.defaultSettings().copy(
    aggregate = aggregateAppSettings,
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys1,
      otherSubsystems = Set(Platform.SubsystemSettings(
        subsystemDeployment = deploymentSubsys2,
        ids = Set(4,5)
      ))
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = Set(1,2,3),
      nbs = Map(1 -> Set(2,4), 2 -> Set(), 3 -> Set())
    )
  )

  val settings2 = settings1.copy(
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys2
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(ids = Set(4,5), nbs = Map())
  )
}
// STEP 3: DEFINE MAIN PROGRAM
object Demo0A_MainProgram_Subsys1 extends Platform.BasicMain(Demo0A_Inputs.settings1)
object Demo0A_MainProgram_Subsys2 extends Platform.BasicMain(Demo0A_Inputs.settings2)
