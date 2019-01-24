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

  class DevInfo(val id: ID, var pos: P, var lsns: Map[LSNS,Any] = Map.empty, var nsns: (NSNS)=>(ID)=>Any){
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
    case class NodeChangeSensor(id : ID, sensor : LSNS, value : Any) extends MetaAction

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
    case class MultiNodeChangeSensor(ids : Set[ID], sensor : LSNS, value : Any) extends MetaAction

    private def computeAction(meta : MetaAction) : Unit = meta match {
      case NodeMovement(id,point) => this.setPosition(id,point)
      case NodeDtMovement(id,dt) => val currentPosition = this.space.getLocation(id)
        this.setPosition(id,Point3D(currentPosition.x + dt._1, currentPosition.y + dt._2, currentPosition.z).asInstanceOf[P])
      case MultiNodeMovement(map) => map.foreach {x => this.setPosition(x._1,x._2)}
      case NodeChangeSensor(id, sensor,value) => this.chgSensorValue(sensor,Set(id),value)
      case _ =>
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

    override def localSensor[A](name: LSNS)(id: ID): A = devs(id).lsns(name).asInstanceOf[A]
    override def addSensor[A](name: LSNS, value: A): Unit = {
      sensors += name -> value
      chgSensorValue(name, devs.keySet, value)
    }

    override def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A): Unit = {
      ids.foreach(x => {
        devs(x).lsns += name -> value
        this.notify(SensorChangedEvent(x,name))
      })
    }

    class SpatialSimulatorContextImpl(id: ID) extends SimulatorContextImpl(id){

      import NetworkSimulator.Optionable

      override def localSensorRetrieve[T](lsns: LSNS, id: ID): Option[T] =  devs.get(id).flatMap{x => x.lsns.get(lsns)}.map{_.asInstanceOf[T]}

      override def nbrSensorRetrieve[T](nsns: NSNS, id: ID, nbr: ID): Option[T] =
        devs.get(id).map(_.nsns(nsns)(nbr)).map(_.asInstanceOf[T])

      override def sense[T](lsns: LSNS): Option[T] = lsns match {
        case LSNS_POSITION => space.getLocation(id).some[T]
        case _ => super.sense(lsns)
      }

      override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] = nsns match {
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
                          lsnsMap: MMap[LSNS,MMap[ID,Any]] = MMap(),
                          nsnsMap: MMap[NSNS,MMap[ID,MMap[ID,Any]]] = MMap(),
                          seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK = {
      val positions = SpaceHelper.gridLocations(gsettings, seeds.configSeed)
      val ids = for(i <- 1 to gsettings.nrows * gsettings.ncols) yield i
      var lsnsById = Map[ID, Map[LSNS,Any]]()
      var nsnsById = Map[ID, Map[NSNS,Any]]()
      for(lsn <- lsnsMap.keys; (dev,v) <- lsnsMap(lsn)) {
        lsnsById += dev -> (lsnsById.getOrElse(dev, Map()) + (lsn -> v))
      }
      for(nsn <- nsnsMap.keys; (dev,v) <- nsnsMap(nsn)) {
        nsnsById += dev -> (nsnsById.getOrElse(dev, Map()) + (nsn -> v))
      }

      val devs: Map[ID,DevInfo] = ((ids map lId.fromNum) zip positions).map {
        case (id, pos) => (id, new DevInfo(id, pos.asInstanceOf[P], lsnsById.getOrElse(id, Map()), sns => nbr => nsnsById.getOrElse(id, Map())(sns)))
      }.toMap
      val space = buildNewSpace(devs mapValues(v => v.pos))
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.gridRepr(gsettings.nrows), seeds.simulationSeed, seeds.randomSensorSeed)
    }

    // TODO: basicSimulator shouldn't use randomness!!! fix it!!!
    override def basicSimulator(idArray: MArray[ID] = MArray(),
                                nbrMap: MMap[ID, Set[ID]] = MMap(),
                                lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                                nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK = {
      val positions = SpaceHelper.randomLocations(SimpleRandomSettings(), idArray.length)

      var lsnsById = Map[ID, Map[LSNS,Any]]()
      var nsnsById = Map[ID, Map[NSNS,Any]]()
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