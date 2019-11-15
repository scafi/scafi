/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.p2p

import akka.actor.Props

trait PlatformDevices extends PlatformBehaviors { self: Platform.Subcomponent =>
  /**
   * Specializes a [[ComputationDeviceActor]] to work in a decentralized,
   *  peer-to-peer manner.
   * In particular, it needs to propagate each computed state to its neighbors.
   */
  class DeviceActor(override val selfId: UID,
                    override var aggregateExecutor: Option[ProgramContract],
                    override var execScope: ExecScope)
    extends P2pBaseDeviceActor
    with MissingCodeManagementBehavior
    with ObservableDeviceActor

  object DeviceActor extends Serializable {
    def props(selfId: UID,
              program: Option[ProgramContract],
              execStrategy: ExecScope): Props =
      Props(classOf[DeviceActor], self, selfId, program, execStrategy)
  }
}
