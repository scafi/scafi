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

package it.unibo.scafi.distrib.actor

import akka.actor.Actor.Receive
import it.unibo.scafi.distrib.actor.patterns.{ObservableActorBehavior, BasicActorBehavior, PeriodicBehavior, LifecycleBehavior}

import akka.actor.{ActorRef, Cancellable, Actor}

import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.duration._

trait PlatformDevices { self: Platform.Subcomponent =>

  /**
   * Defines a device lifecycle behavior that depends on the field {{execScope}.
   * The characteristic is that the behavior is dynamic, namely, can
   *  accomodate change to its execution scope.
   * (See [[ExecScope]])
   */
  trait DynamicDeviceLifecycleBehavior extends LifecycleBehavior
    with PeriodicBehavior { thisVery: Actor =>

    // ABSTRACT FIELDS

    var execScope: ExecScope

    // CONCRETE FIELDS

    var first = false
    var tick: Option[Cancellable] = None

    override val initialDelay = execScope match {
      case DeviceDelegated(devExec) => devExec match {
        case PeriodicDeviceExecStrategy(initial,_) => initial
        case DelayedDeviceExecStrategy(initial,_) => initial
        case _ => None
      }
      case _ => None
    }

    // OVERRIDES TO CUSTOM LIFECYCLE

    override var workInterval = 1.day // Ignored in HandleLifecycle()
    import context.dispatcher

    override def handleLifecycle(): Unit = execScope match {
      case DeviceDelegated(strategy) => strategy match {
        case DelayedDeviceExecStrategy(_,delay) => {
          scheduleNextWorkingCycle(delay)
        }
        case PeriodicDeviceExecStrategy(_,period) => {
          tick = tick.orElse(Some(context.system.scheduler.schedule(period,period,self,GoOn)))
        }
        case _ => // Do nothing
      }
      case _ => // Do nothing
    }

    override def lifecyclePreStart(): Unit = execScope match {
      case DeviceDelegated(strategy) => strategy match {
        case DelayedDeviceExecStrategy(_,_)
             | PeriodicDeviceExecStrategy(_,_) => {
          // Reuse default periodic behavior pre-start
          // which essentially schedules a cycle with 'initialDelay' (if set)
          periodicBehaviorPreStart()
        }
        case _ => // Do nothing
      }
      case _ => // Do nothing
    }

    override def lifecyclePostStop(): Unit = {
      super.lifecyclePostStop()
      tick.foreach(_.cancel())
    }
  }

  /**
   * Defines the basic sensing behavior of a device.
   * It might be used to represent a "minimal" device that performs
   *  no computation locally.
   */
  trait SensingBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS

    val sensorValues = MMap[LSNS, Any]()
    val nbrSensorValues = MMap[NSNS, MMap[ID, Any]]()

    // REACTIVE BEHAVIOR

    def sensingBehavior: Receive = {
      case MsgLocalSensorValue(name, value) => setLocalSensorValue(name, value)
      case MsgNbrSensorValue(name, map) => setNbrSensorValue(name, map)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(sensingBehavior)

    // BEHAVIOR METHODS

    def setLocalSensorValue(name: LSNS, value: Any): Unit = {
      sensorValues += name -> value
    }

    def setNbrSensorValue(name: NSNS, map: Map[ID,Any]): Unit =
      nbrSensorValues(name) = MMap(map.toSeq:_*)
  }

  /**
   * Defines a behavior for managing sensors.
   */
  trait SensorManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS

    val localSensors = MMap[LSNS, ()=>Any]()

    // REACTIVE BEHAVIOR

    def sensorManagementBehavior: Receive = {
      case MsgAddPushSensor(ref) => { ref ! MsgAddObserver(self); ref ! GoOn }
      case MsgAddSensor(name, provider) => setLocalSensor(name, provider)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(sensorManagementBehavior)

    // BEHAVIOR METHODS

    def setLocalSensor(name: LSNS, provider: ()=>Any): Unit =
      localSensors += (name -> provider)
  }

  /**
   * Defines a behavior for managing actuators.
   */
  trait ActuatorManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS

    val actuators = MMap[LSNS, (Any)=>Unit]()

    // REACTIVE BEHAVIOR

    def actuatorManagementBehavior: Receive = {
      case MsgAddActuator(name, consumer) => setActuator(name, consumer)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(actuatorManagementBehavior)

    // BEHAVIOR METHODS

    def setActuator(name: LSNS, consumer: (Any)=>Unit): Unit =
      actuators += name -> consumer

    def executeActuators(value: Any): Unit =
      actuators.foreach{ case (name,consumer) => consumer(value) }
  }

  /**
   * Defines a behavior for managing neighbours' info.
   */
  trait BaseNbrManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS
    var nbrs = Map[ID,NbrInfo]()

    // BEHAVIOR METHODS
    def mergeNeighborInfo(idn: ID, info: NbrInfo): Unit = {
      var toAdd = this.nbrs.get(idn)
        .orElse(Some(NbrInfo(idn,None,None,None))).get
      // No let's merge, by assuming that 'info' has is more up-to-date
      toAdd = toAdd.copy(export = info.export.orElse(toAdd.export),
        mailbox = info.mailbox.orElse(toAdd.mailbox),
        path = info.path.orElse(toAdd.path))
      this.nbrs += idn -> toAdd
    }

    def updateNeighborhood(neighbors: Set[ID], clear: Boolean = false): Unit = {
      logger.debug(s"\nAdding neighbors $neighbors and clear=$clear")

      if(clear) this.nbrs = Map()

      neighbors.foreach(nbr => {
        this.nbrs += nbr -> nbrs.getOrElse(nbr, NbrInfo(nbr,None,None))
      })
    }

    def updateNeighborsState(exps: Map[ID,Option[EXPORT]], clear: Boolean = false): Unit = {
      logger.debug(s"\nAdding neighbors' exports $exps and clear=$clear")

      if(clear) this.nbrs = Map()

      exps.foreach {
        case (idn, expn) => {
          this.nbrs += idn -> this.nbrs.getOrElse(idn, NbrInfo(idn, None, None)).copy(export = expn)
        }
      }
    }

    def removeNeighbor(idn: ID): Unit = {
      logger.debug(s"\nRemoving neighbor $idn")
      nbrs -= idn
    }
  }

  /**
   * Base trait for all device actors.
   */
  trait BaseDeviceActor extends Actor {
    val selfId: ID
  }

  /**
   * Defines a "full" device that, in addition to sensing ([[SensingBehavior]]),
   *  it also performs a computation, locally, using an {{aggregateExecutor}}.
   */
  trait ComputationDeviceActor extends BaseDeviceActor
  with BasicActorBehavior
  with SensingBehavior
  with SensorManagementBehavior
  with ActuatorManagementBehavior
  with BaseNbrManagementBehavior {

    // ABSTRACT MEMBERS

    def propagateExportToNeighbors(export: EXPORT)
    var aggregateExecutor: Option[ExecutionTemplate]

    // CONCRETE FIELDS

    var rounds = 0
    var lastExport: Option[EXPORT] = None

    // ACTOR LIFECYCLE

    override def preStart(): Unit = {
      super.preStart()
      logger.info(s"\nHello. I am ${selfId} and I am about to start.\n" +
        s"My program is $aggregateExecutor\n")
    }

    // CALLBACKS

    def beforeJob(): Unit = { }
    def afterJob(): Unit = { }

    // REACTIVE BEHAVIOR

    override def workingBehavior: Receive = {
      case GoOn => {
        beforeJob()
        doJob()
        afterJob()
      }
    }

    // BEHAVIOR METHODS

    def doJob(): Unit = aggregateExecutor.foreach { program =>
      rounds = rounds + 1

      var nbrExports = nbrs.filter(_._2.export.isDefined).mapValues(_.export.get)
      // Include the previous export for the current device
      lastExport.foreach(le => nbrExports += (selfId -> le))

      // Query local sensor to update sensor values
      updateSensorValues()

      val context = new ContextImpl(
        selfId,
        nbrExports,
        sensorValues,
        nbrSensorValues)
      val export = compute(context)
      this.lastExport = Some(export)

      logger.debug(s"\nExecuted round $rounds => $export\nBY $context\n$nbrs")

      propagateExportToNeighbors(export)

      executeActuators(export.root())
    }

    def compute(ctx: CONTEXT): EXPORT = {
      val exp = aggregateExecutor.get.round(ctx)
      exp
    }

    def updateSensorValues(): Unit = localSensors.foreach { case (name,provider) =>
      setLocalSensorValue(name, provider())
    }
  }

  /**
   * It extends a [[ComputationDeviceActor]] with a [[DynamicDeviceLifecycleBehavior]].
   * In other words, the computation device has a (possibly dynamic) lifecycle
   *  that depends on the current execution scope (see [[ExecScope]]).
   */
  trait DynamicComputationDeviceActor
    extends ComputationDeviceActor with DynamicDeviceLifecycleBehavior {
    override def preStart(): Unit = {
      super.preStart()
      lifecyclePreStart()
    }

    override def afterJob(): Unit = {
      super.afterJob()
      handleLifecycle()
    }
  }

  /**
   * Defines the query management behavior of a "queryable" device, i.e.,
   *  a device that can be asked/queried for information.
   */
  trait QueryableDeviceActorBehavior extends BasicActorBehavior { baseDevice: ComputationDeviceActor =>
    override def queryManagementBehavior: Receive = super.queryManagementBehavior.orElse {
      case MsgGetNeighbors => sender ! nbrs
      case MsgGetExport => sender ! lastExport
      case MsgGetSensorValue(lsns) => sender ! sensorValues(lsns)
      case MsgGetNbrSensorValue(nsns,idn) => sender ! nbrSensorValues(nsns)(idn)
    }
  }

  /**
   * Extends a [[ComputationDeviceActor]] with an [[ObservableActorBehavior]].
   * In other wards, it defines a computation device actors that is also
   *  observable, that is, can handle a set of observers and can notify them
   *  when certain events occur.
   * In particular, observers are notified:
   *   - When the actor starts (presentation message [[MyNameIs]])
   *   - After a computation (propagating round number [[MsgRound]] and computed result/state [[MsgExport]])
   *   - When the device's neighborhood [[MsgNeighborhood]] or the device's view of
   *     the neighborhood state [[MsgExports]] changes
   */
  trait ObservableDeviceActor extends ComputationDeviceActor with ObservableActorBehavior {
    override def receive: Receive = super.receive.orElse(observersManagementBehavior)

    override def afterJob(): Unit = {
      super.afterJob()
      notifyObservers(MsgRound(selfId, this.rounds))
      notifyObservers(MsgExport(selfId, this.lastExport.get))
    }

    override def observerAdded(ref: ActorRef): Unit = {
      ref ! MyNameIs(selfId)
      ref ! MsgNeighborhood(selfId, this.nbrs.keySet)
    }

    override def setLocalSensorValue(name: LSNS, value: Any): Unit = {
      super.setLocalSensorValue(name, value)
      notifyObservers(MsgLocalSensorValue(name, value))
    }

    override def updateNeighborhood(neighbors: Set[ID], clear: Boolean = false): Unit = {
      super.updateNeighborhood(neighbors, clear)
      notifyObservers(MsgNeighborhood(selfId, this.nbrs.keySet))
    }

    override def removeNeighbor(idn: ID): Unit = {
      super.removeNeighbor(idn)
      notifyObservers(MsgNeighborhood(selfId, this.nbrs.keySet))
    }

    override def updateNeighborsState(nexps: Map[ID,Option[EXPORT]], clear: Boolean = false): Unit = {
      super.updateNeighborsState(nexps, clear)

      // Trailing .map(identity) is needed because mapValues() results in a non-serializable object
      // See: http://stackoverflow.com/questions/17709995/notserializableexception-for-mapstring-string-alias
      notifyObservers(MsgExports(this.nbrs.filter(_._2.export.isDefined).
        mapValues(_.export.get).map(identity)))
    }
  }
}
