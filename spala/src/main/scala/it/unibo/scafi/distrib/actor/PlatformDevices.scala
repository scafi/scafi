/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.actor.patterns.{BasicActorBehavior, LifecycleBehavior, ObservableActorBehavior, PeriodicBehavior}
import akka.actor.{Actor, ActorRef, Cancellable}

import scala.collection.mutable.{Map => MMap}
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

    val sensorValues = MMap[LSensorName, Any]()
    val nbrSensorValues = MMap[NSensorName, MMap[UID, Any]]()

    // REACTIVE BEHAVIOR

    def sensingBehavior: Receive = {
      case MsgLocalSensorValue(name, value) => setLocalSensorValue(name, value)
      case MsgNbrSensorValue(name, map) => setNbrSensorValue(name, map)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(sensingBehavior)

    // BEHAVIOR METHODS

    def setLocalSensorValue(name: LSensorName, value: Any): Unit = {
      sensorValues += name -> value
    }

    def setNbrSensorValue(name: NSensorName, map: Map[UID,Any]): Unit =
      nbrSensorValues(name) = MMap(map.toSeq:_*)
  }

  /**
   * Defines a behavior for managing sensors.
   */
  trait SensorManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS

    val localSensors = MMap[LSensorName, ()=>Any]()

    // REACTIVE BEHAVIOR

    def sensorManagementBehavior: Receive = {
      case MsgAddPushSensor(ref) => { ref ! MsgAddObserver(self); ref ! GoOn }
      case MsgAddSensor(name, provider) => setLocalSensor(name, provider)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(sensorManagementBehavior)

    // BEHAVIOR METHODS

    def setLocalSensor(name: LSensorName, provider: ()=>Any): Unit =
      localSensors += (name -> provider)
  }

  /**
   * Defines a behavior for managing actuators.
   */
  trait ActuatorManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS

    val actuators = MMap[LSensorName, (Any)=>Unit]()

    // REACTIVE BEHAVIOR

    def actuatorManagementBehavior: Receive = {
      case MsgAddActuator(name, consumer) => setActuator(name, consumer)
    }

    override def inputManagementBehavior: Receive =
      super.inputManagementBehavior.orElse(actuatorManagementBehavior)

    // BEHAVIOR METHODS

    def setActuator(name: LSensorName, consumer: (Any)=>Unit): Unit =
      actuators += name -> consumer

    def executeActuators(value: Any): Unit =
      actuators.foreach{ case (name,consumer) => consumer(value) }
  }

  /**
   * Defines a behavior for managing neighbours' info.
   */
  trait BaseNbrManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    // FIELDS
    var nbrs = Map[UID,NbrInfo]()

    // BEHAVIOR METHODS
    def mergeNeighborInfo(idn: UID, info: NbrInfo): Unit = {
      var toAdd = this.nbrs.get(idn)
        .orElse(Some(NbrInfo(idn,None,None,None))).get
      // No let's merge, by assuming that 'info' has is more up-to-date
      toAdd = toAdd.copy(export = info.export.orElse(toAdd.export),
        mailbox = info.mailbox.orElse(toAdd.mailbox),
        path = info.path.orElse(toAdd.path))
      this.nbrs += idn -> toAdd
    }

    def updateNeighborhood(neighbors: Set[UID], clear: Boolean = false): Unit = {
      logger.debug(s"\nAdding neighbors $neighbors and clear=$clear")

      if(clear) this.nbrs = Map()

      neighbors.foreach(nbr => {
        this.nbrs += nbr -> nbrs.getOrElse(nbr, NbrInfo(nbr,None,None))
      })
    }

    def updateNeighborsState(exps: Map[UID,Option[ComputationExport]], clear: Boolean = false): Unit = {
      logger.debug(s"\nAdding neighbors' exports $exps and clear=$clear")

      if(clear) this.nbrs = Map()

      exps.foreach {
        case (idn, expn) => {
          this.nbrs += idn -> this.nbrs.getOrElse(idn, NbrInfo(idn, None, None)).copy(export = expn)
        }
      }
    }

    def removeNeighbor(idn: UID): Unit = {
      logger.debug(s"\nRemoving neighbor $idn")
      nbrs -= idn
    }
  }

  /**
   * Base trait for all device actors.
   */
  trait BaseDeviceActor extends Actor {
    val selfId: UID
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

    def propagateMsgToNeighbors(msg: Any)
    var aggregateExecutor: Option[ProgramContract]

    // CONCRETE FIELDS

    var rounds = 0
    var lastExport: Option[ComputationExport] = None

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

      val context = dataFactory.context(
        selfId,
        nbrExports,
        sensorValues.toMap,
        nbrSensorValues.mapValues(_.toMap).toMap)
      val export = compute(context)
      this.lastExport = Some(export)

      logger.debug(s"\nExecuted round $rounds => $export\nBY $context\n$nbrs")

      propagateExportToNeighbors(export)

      executeActuators(export.root())
    }

    def compute(ctx: ComputationContext): ComputationExport = {
      val exp = aggregateExecutor.get.round(ctx)
      exp
    }

    def propagateExportToNeighbors(export: ComputationExport): Unit =
      propagateMsgToNeighbors(MsgExport(selfId, export))

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

  trait WeakCodeMobilityDeviceActor extends ComputationDeviceActor {
    //FIELDS
    var lastProgram: Option[()=>Any] = None
    var unreliableNbrs: Set[UID] = Set()

    // REACTIVE BEHAVIOR
    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
      case MsgUpdateProgram(nid, program) => handleProgram(nid, program)
    }

    override def beforeJob(): Unit = {
      super.beforeJob()
      if (lastExport.isDefined) {
        // remove neighbors' exports that cannot be merged with the last export
        nbrs = nbrs ++ nbrs
          .filter(_._2.export.isDefined)
          .filterNot(n => n._2.export.get.root().getClass == lastExport.get.root().getClass)
          .map { case (id, NbrInfo(idn, _, mailbox, path)) => id -> NbrInfo(idn, None, mailbox, path) }
        // remove exports that come from unreliable neighbors
        nbrs = nbrs ++ nbrs.filter(n => unreliableNbrs.contains(n._1)).map {
          case (id, NbrInfo(idn, _, mailbox, path)) => id -> NbrInfo(idn, None, mailbox, path)
        }
      } else {
        // remove all exports
        nbrs = nbrs ++ nbrs.map { case (id, NbrInfo(idn, _, mailbox, path)) => id -> NbrInfo(idn, None, mailbox, path) }
      }
    }

    // BEHAVIOR METHODS
    def handleProgram(nid: UID, program: () => Any): Unit = {
      if (lastProgram.isEmpty || lastProgram.get != program) {
        logger.debug(s"\nProgram updated => $program")
        lastProgram = Some(program)
        unreliableNbrs = nbrs.keySet
        resetComputationState()
        updateProgram(program)
        propagateProgramToNeighbors(program)
      }
      unreliableNbrs = unreliableNbrs - nid
    }
    def resetComputationState(): Unit = {
      lastExport = None
      nbrs = nbrs.map { case (id, NbrInfo(idn, _, mailbox, path)) => id -> NbrInfo(idn, None, mailbox, path) }
    }
    def updateProgram(program: () => Any): Unit = program() match {
      case pc: ProgramContract => aggregateExecutor = Some(pc)
    }
    def propagateProgramToNeighbors(program: () => Any): Unit =
      propagateMsgToNeighbors(MsgUpdateProgram(selfId, program))
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

    override def setLocalSensorValue(name: LSensorName, value: Any): Unit = {
      super.setLocalSensorValue(name, value)
      notifyObservers(MsgLocalSensorValue(name, value))
    }

    override def updateNeighborhood(neighbors: Set[UID], clear: Boolean = false): Unit = {
      super.updateNeighborhood(neighbors, clear)
      notifyObservers(MsgNeighborhood(selfId, this.nbrs.keySet))
    }

    override def removeNeighbor(idn: UID): Unit = {
      super.removeNeighbor(idn)
      notifyObservers(MsgNeighborhood(selfId, this.nbrs.keySet))
    }

    override def updateNeighborsState(nexps: Map[UID,Option[ComputationExport]], clear: Boolean = false): Unit = {
      super.updateNeighborsState(nexps, clear)

      // Trailing .map(identity) is needed because mapValues() results in a non-serializable object
      // See: http://stackoverflow.com/questions/17709995/notserializableexception-for-mapstring-string-alias
      notifyObservers(MsgExports(this.nbrs.filter(_._2.export.isDefined).
        mapValues(_.export.get).map(identity)))
    }
  }
}
