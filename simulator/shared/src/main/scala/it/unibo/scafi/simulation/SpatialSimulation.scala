/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}
import it.unibo.scafi.platform.{SimulationPlatform, SpaceAwarePlatform}
import it.unibo.scafi.simulation.MetaActionManager.MetaAction
import it.unibo.scafi.simulation.SimulationObserver.{MovementEvent, SensorChangedEvent}
import it.unibo.scafi.space._

import scala.collection.immutable.Queue
import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}

trait SpatialSimulation extends Simulation with SpaceAwarePlatform  {
  self: SimulationPlatform.PlatformDependency with BasicSpatialAbstraction =>

  class DevInfo(val id: ID, var pos: P, var lsns: Map[CNAME,Any] = Map.empty, var nsns: (CNAME)=>(ID)=>Any){
    override def toString: String = s"Device[id: $id, pos: $pos]"
  }

  class SpaceAwareSimulator(
                             val space: SPACE[ID],
                             val devs: Map[ID, DevInfo],
                             toStr: NetworkSimulator => String = SpaceAwareSimulator.defaultRepr,
                             simulationSeed: Long,
                             randomSensorSeed: Long
  ) extends NetworkSimulator (
      idArray = MArray(devs.keys.toSeq:_*),
      toStr = toStr,
      simulationSeed = simulationSeed,
      randomSensorSeed = randomSensorSeed
    )  with MetaActionManager {

    /**
      * meta action used to move a node into another position
      * @param id the node id
      * @param point the new node position
      */
    case class NodeMovement(id : ID, point : P) extends MetaAction

    /**
      * meta action used to move node using a dt movement
      * @param id the node id
      * @param dt the delta movement
      */
    case class NodeDtMovement(id : ID, dt : (Double,Double)) extends MetaAction

    /**
      * meta action used to change the value of a sensor
      * @param id the node id
      * @param sensor the sensor name
      * @param value the new value of sensor
      */
    case class NodeChangeSensor(id : ID, sensor : CNAME, value : Any) extends MetaAction

    /**
      * meta action used to move a set of node
      * @param movementMap the map of node id and new node position
      */
    case class MultiNodeMovement(movementMap : Map[ID,P]) extends MetaAction

    /**
      * a meta action used to change a set of node sensor value
      * @param ids the id of node
      * @param sensor the sensor changed
      * @param value the new sensor value
      */
    case class MultiNodeChangeSensor(ids : Set[ID], sensor : CNAME, value : Any) extends MetaAction

    private def computeAction(meta : MetaAction) : Unit = meta match {
      case NodeMovement(id,point) => this.setPosition(id,point)
      case NodeDtMovement(id,dt) => val currentPosition = this.space.getLocation(id)
        this.setPosition(id,Point3D(currentPosition.x + dt._1, currentPosition.y + dt._2, currentPosition.z).asInstanceOf[P])
      case MultiNodeMovement(map) => map.foreach {x => this.setPosition(x._1,x._2)}
      case NodeChangeSensor(id, sensor,value) => this.chgSensorValue(sensor,Set(id),value)
      case _ => throw new IllegalArgumentException(s"Meta action ${meta} not supported by the spatial simulator.")
    }

    override def process(): Unit = {
      var toProcess : List[MetaAction] = List.empty
      while(actionQueue.nonEmpty) {
        toProcess = actionQueue.dequeue() :: toProcess
      }
      toProcess.foreach {_ match {
        case MetaActionManager.MultiAction(actions @ _*) => actions.foreach(computeAction(_))
        case action => computeAction(action)
      }}
    }

    override val ids: Set[ID] = devs.keySet
    override def neighbourhood(id: ID): Set[ID] = space.getNeighbors(id).toSet

    def setPosition(id: ID, newPos: P): Unit = {
      devs(id).pos = newPos
      space.setLocation(id,newPos)
      this.notify(MovementEvent(id))
    }

    def getAllNeighbours(): Map[ID, Iterable[ID]] =
      ids.iterator.map(id => id -> space.getNeighbors(id)).toMap

    override def localSensor[A](name: CNAME)(id: ID): A = devs(id).lsns(name).asInstanceOf[A]
    override def addSensor[A](name: CNAME, value: A): Unit = {
      sensors += name -> value
      chgSensorValue(name, devs.keySet, value)
    }

    override def chgSensorValue[A](name: CNAME, ids: Set[ID], value: A): Unit = {
      ids.foreach(x => {
        devs(x).lsns += name -> value
        this.notify(SensorChangedEvent(x,name))
      })
    }

    class SpatialSimulatorContextImpl(id: ID) extends SimulatorContextImpl(id){

      import NetworkSimulator.Optionable

      override def localSensorRetrieve[T](lsns: CNAME, id: ID): Option[T] =  devs.get(id).flatMap{ x => x.lsns.get(lsns)}.map{_.asInstanceOf[T]}

      override def nbrSensorRetrieve[T](nsns: CNAME, id: ID, nbr: ID): Option[T] =
        devs.get(id).map(_.nsns(nsns)(nbr)).map(_.asInstanceOf[T])

      override def sense[T](lsns: CNAME): Option[T] = lsns match {
        case LSNS_POSITION => space.getLocation(id).some[T]
        case _ => super.sense(lsns)
      }

      override def nbrSense[T](nsns: CNAME)(nbr: ID): Option[T] = nsns match {
        case NBR_RANGE =>
          space.getDistance(space.getLocation(selfId), space.getLocation(nbr)).some[T]
        case NBR_VECTOR => {
          val (mypos, npos) = (space.getLocation(selfId), space.getLocation(nbr))
          Point3D(npos.x-mypos.x, npos.y-mypos.y, npos.z-mypos.z).some[T]
        }
        case _ => super.nbrSense(nsns)(nbr)
      }
    }

    override def context(id: ID): CONTEXT =
      new SpatialSimulatorContextImpl(id)
  }

  object SpaceAwareSimulator {
    def defaultRepr(_net: NetworkSimulator): String = {
      val net = _net.asInstanceOf[SpaceAwareSimulator]
      net.idArray.sortBy(net.space.getLocation(_)).map {
        i => net.export(i).map { e => s"$i@${net.space.getLocation(i)}(${e.root()})" }.getOrElse("_")
      }.mkString("", "\t", "")
    }

    def gridRepr(numCols: Int)(_net: NetworkSimulator): String = {
      val net = _net.asInstanceOf[SpaceAwareSimulator]

      net.idArray.sortBy(net.space.getLocation(_)).map {
        i => net.export(i).map { e => s"$i@${net.space.getLocation(i)}(${e.root()})" }.getOrElse("_")
      }
        .zipWithIndex
        .map(z => (if (z._2 % numCols == 0) "\n" else "") + z._1)
        .mkString("", "\t", "")
    }
  }

  override def simulatorFactory: SimulatorFactory = new BasicSimulatorFactory {
    override def gridLike(gsettings: GridSettings,
                          rng: Double,
                          lsnsMap: MMap[CNAME,MMap[ID,Any]] = MMap(),
                          nsnsMap: MMap[CNAME,MMap[ID,MMap[ID,Any]]] = MMap(),
                          seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK = {
      val positions = SpaceHelper.gridLocations(gsettings, seeds.configSeed)
      val ids = for(i <- 1 to gsettings.nrows * gsettings.ncols) yield i
      var lsnsById = Map[ID, Map[CNAME,Any]]()
      var nsnsById = Map[ID, Map[CNAME,Any]]()
      for(lsn <- lsnsMap.keys; (dev,v) <- lsnsMap(lsn)) {
        lsnsById += dev -> (lsnsById.getOrElse(dev, Map()) + (lsn -> v))
      }
      for(nsn <- nsnsMap.keys; (dev,v) <- nsnsMap(nsn)) {
        nsnsById += dev -> (nsnsById.getOrElse(dev, Map()) + (nsn -> v))
      }

      val devs: Map[ID,DevInfo] = ((ids map lId.fromNum) zip positions).map {
        case (id, pos) => (id, new DevInfo(id, pos.asInstanceOf[P], lsnsById.getOrElse(id, Map()), sns => nbr => nsnsById.getOrElse(id, Map())(sns)))
      }.toMap
      val space = new Basic3DSpace(devs.mapValues(v => v.pos).toMap, rng)
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.gridRepr(gsettings.nrows), seeds.simulationSeed, seeds.randomSensorSeed)
    }

    // TODO: basicSimulator shouldn't use randomness!!! fix it!!!
    override def basicSimulator(idArray: MArray[ID] = MArray(),
                                nbrMap: MMap[ID, Set[ID]] = MMap(),
                                lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
                                nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK = {
      val positions = SpaceHelper.randomLocations(SimpleRandomSettings(), idArray.length)

      var lsnsById = Map[ID, Map[CNAME,Any]]()
      var nsnsById = Map[ID, Map[CNAME,Any]]()
      for(lsn <- lsnsMap.keys; (dev,v) <- lsnsMap(lsn)) {
        lsnsById += dev -> (lsnsById.getOrElse(dev, Map()) + (lsn -> v))
      }
      for(nsn <- nsnsMap.keys; (dev,v) <- nsnsMap(nsn)) {
        nsnsById += dev -> (nsnsById.getOrElse(dev, Map()) + (nsn -> v))
      }

      val devs: Map[ID,DevInfo] = (idArray zip positions).map {
        case (id, pos) => (id, new DevInfo(id, pos.asInstanceOf[P], lsnsById.getOrElse(id, Map()), sns => nbr => nsnsById.getOrElse(id, Map())(sns) ))
      }.toMap
      val space = buildNewSpace(devs mapValues(v => v.pos))
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.defaultRepr, SIM_SEED, RANDOM_SENSOR_SEED)
    }
  }
}