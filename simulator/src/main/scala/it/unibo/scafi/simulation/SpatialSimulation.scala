package it.unibo.scafi.simulation

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}
import it.unibo.scafi.platform.Platform
import it.unibo.scafi.space._

import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}

/**
 * @author Roberto Casadei
 *
 */

trait SpatialSimulation extends Simulation { self: Platform.PlatformDependency with MetricSpatialAbstraction =>
  case class DevInfo(id: ID, pos: P, lsns: Map[LSNS, Any], nsns: Map[NSNS, Any])

  private class SpaceAwareSimulator(
    val space: SPACE[ID],
    val devs: Map[ID, DevInfo],
    toStr: NetworkSimulator => String = SpaceAwareSimulator.DefaultRepr
  ) extends NetworkSimulator(idArray = MArray(devs.keys.toSeq:_*), toStr = toStr) {
    override val ids: Set[ID] = devs.keySet
    override def neighbourhood(id: ID): Set[ID] = space.getNeighbors(id).toSet
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

  override def simulatorFactory = new SimulatorFactory {
    def gridLike(n: Int,
                 m: Int,
                 stepx: Double = 1,
                 stepy: Double = 1,
                 eps: Double = 0.0,
                 rng: Double,
                 lsnsMap: MMap[LSNS,MMap[ID,Any]] = MMap(),
                 nsnsMap: MMap[NSNS,MMap[ID,MMap[ID,Any]]] = MMap())
                (implicit nbrRangeName: NSNS, lId: Linearizable[ID]): NETWORK = {
      val positions = SpaceHelper.GridLocations(GridSettings(n,m,stepx,stepy,eps))
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
        case (id:ID, pos:P) => (id, DevInfo(id, pos, lsnsById.getOrElse(id, Map()), nsnsById.getOrElse(id, Map())))
      }.toMap
      println(devs)
      val space = buildNewSpace(devs mapValues(v => v.pos))
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.GridRepr(n))
    }

    def basicSimulator(idArray: MArray[ID] = MArray(),
                       nbrMap: MMap[ID, Set[ID]] = MMap(),
                       lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                       nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()
                       ): NETWORK = {
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
        case (id:ID, pos:P) => (id, DevInfo(id, pos, lsnsById(id), nsnsById(id)))
      }.toMap
      val space = buildNewSpace(devs mapValues(v => v.pos))
      new SpaceAwareSimulator(space, devs, SpaceAwareSimulator.DefaultRepr)
    }
  }

}
