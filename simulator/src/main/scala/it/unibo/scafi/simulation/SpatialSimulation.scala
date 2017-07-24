package it.unibo.scafi.simulation

import java.time.LocalTime
import java.util.concurrent.TimeUnit

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}
import it.unibo.scafi.platform.{Platform, SimulationPlatform, SpaceAwarePlatform}
import it.unibo.scafi.space._

import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}
import scala.concurrent.duration.Duration

/**
 * @author Roberto Casadei
 *
 */

trait SpatialSimulation extends Simulation with SpaceAwarePlatform  {
  self: SimulationPlatform.PlatformDependency with BasicSpatialAbstraction =>

  class DevInfo(val id: ID, var pos: P, var lsns: LSNS=>Any, var nsns: (NSNS)=>(ID)=>Any){
    override def toString: String = s"Device[id: $id, pos: $pos]"
  }

  class SpaceAwareSimulator(
    val space: SPACE[ID],
    val devs: Map[ID, DevInfo],
    toStr: NetworkSimulator => String = SpaceAwareSimulator.DefaultRepr,
    simulationSeed: Long,
    randomSensorSeed: Long
  ) extends NetworkSimulator(
      idArray = MArray(devs.keys.toSeq:_*),
      toStr = toStr,
      simulationSeed = simulationSeed,
      randomSensorSeed = randomSensorSeed
    ) {

    override val ids: Set[ID] = devs.keySet
    override def neighbourhood(id: ID): Set[ID] = space.getNeighbors(id).toSet

    def setPosition(id: ID, newPos: P)(implicit ev: space.type <:< MutableSpace[ID]) = {
      devs(id).pos = newPos
      space.asInstanceOf[MutableSpace[ID]].setLocation(id,newPos)
    }

    override def addSensor[A](name: LSNS, value: A): Unit = {
      sensors += name -> value
      chgSensorValue(name, devs.keySet, value)
    }

    override def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A): Unit = {
      ids.foreach(id => {
        val f = devs(id).lsns
        devs(id).lsns = sname => if(name==sname) value else f(sname)
      })
    }

    /*
    override def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A): Unit = {
      ids.foreach { id => lsnsMap += name -> (lsnsMap.getOrElse(name,MMap()) + (id -> value)) }
    }
     */

    class SpatialSimulatorContextImpl(id: ID) extends SimulatorContextImpl(id){

      import NetworkSimulator.Optionable

      override def localSensorRetrieve[T](lsns: LSNS, id: ID): Option[T] =
        lsnsMap.get(lsns).flatMap(_.get(selfId)).orElse(devs.get(id).map(_.lsns(lsns))).map(_.asInstanceOf[T])

      override def nbrSensorRetrieve[T](nsns: NSNS, id: ID, nbr: ID): Option[T] =
        devs.get(id).map(_.nsns(nsns)(nbr)).map(_.asInstanceOf[T])

      override def sense[T](lsns: LSNS): Option[T] = lsns match {
        case LSNS_POSITION => space.getLocation(id).some[T]
        case _ => super.sense(lsns)
      }

      override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] = nsns match {
        case NBR_RANGE_NAME =>
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
    def DefaultRepr(_net: NetworkSimulator): String = {
      val net = _net.asInstanceOf[SpaceAwareSimulator]
      net.idArray.sortBy(net.space.getLocation(_)).map {
        i => net.export(i).map { e => s"$i@${net.space.getLocation(i)}(${e.root()})" }.getOrElse("_")
      }.mkString("", "\t", "")
    }

    def GridRepr(numCols: Int)(_net: NetworkSimulator): String = {
      val net = _net.asInstanceOf[SpaceAwareSimulator]

      net.idArray.sortBy(net.space.getLocation(_)).map {
        i => net.export(i).map { e => s"$i@${net.space.getLocation(i)}(${e.root()})" }.getOrElse("_")
      }
        .zipWithIndex
        .map(z => (if (z._2 % numCols == 0) "\n" else "") + z._1)
        .mkString("", "\t", "")
    }
  }

  override def simulatorFactory = new BasicSimulatorFactory {
    override def gridLike(n: Int,
                 m: Int,
                 stepx: Double = 1,
                 stepy: Double = 1,
                 eps: Double = 0.0,
                 rng: Double,
                 lsnsMap: MMap[LSNS,MMap[ID,Any]] = MMap(),
                 nsnsMap: MMap[NSNS,MMap[ID,MMap[ID,Any]]] = MMap(),
                 seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK = {
      val positions = SpaceHelper.GridLocations(GridSettings(n,m,stepx,stepy,eps), seeds.configSeed)
      val ids = for(i <- 1 to n*m) yield i
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
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.GridRepr(n), seeds.simulationSeed, seeds.randomSensorSeed)
    }

    // TODO: basicSimulator shouldn't use randomness!!! fix it!!!
    override def basicSimulator(idArray: MArray[ID] = MArray(),
                       nbrMap: MMap[ID, Set[ID]] = MMap(),
                       lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                       nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK = {
      val positions = SpaceHelper.RandomLocations(SimpleRandomSettings(), idArray.length)

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
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.DefaultRepr, SIM_SEED, RANDOM_SENSOR_SEED)
    }
  }

}
