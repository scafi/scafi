/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.platform.SimulationPlatform
import it.unibo.utils.observer.{SimpleSource, Source}

import scala.collection.immutable.{Queue, Map => IMap}
import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
 *
 *         This trait defines a component that extends a Platform and
 *         requires to be "attached" to an Engine.
 *         It defines a trait with a simulator skeleton along with two settings
 *         for it, created by a factory.
 *
 */

trait Simulation extends SimulationPlatform { self: SimulationPlatform.PlatformDependency =>

  override type NETWORK = Network with SimulatorOps

  def simulatorFactory: SimulatorFactory = new BasicSimulatorFactory

  trait SimulatorOps {
    self: Network =>
    def context(id: ID): CONTEXT

    def addSensor[A](name: CNAME, value: A): Unit

    def chgSensorValue[A](name: CNAME, ids: Set[ID], value: A): Unit

    def clearExports(): Unit

    def exec(node: EXECUTION, exp: => Any, id: ID): Unit

    def execMany(node: EXECUTION, exp: => Any, size: Int, action: (Network, Int) => Unit): Seq[ID]

    def executeMany(node: EXECUTION, size: Int, action: (Network, Int) => Unit): Seq[ID]

    def execInOrderAndReturn(node: EXECUTION, exp: => Any, firingSeq: Seq[ID]): NETWORK

    def exec(ap: CONTEXT=>EXPORT): (ID,EXPORT)

    def valueMap[T](): Map[ID, T] = this.exports().mapValues(_.get.root().asInstanceOf[T]).toMap
  }

  case class Seeds(configSeed: Long = System.currentTimeMillis(),
                   simulationSeed: Long = System.currentTimeMillis(),
                   randomSensorSeed: Long = System.currentTimeMillis())

  trait SimulatorFactory {
    lazy val CONFIG_SEED: Long = System.currentTimeMillis()
    lazy val SIM_SEED: Long = System.currentTimeMillis()
    lazy val RANDOM_SENSOR_SEED: Long = System.currentTimeMillis()

    def basicSimulator(idArray: MArray[ID] = MArray(),
                       nbrMap: MMap[ID, Set[ID]] = MMap(),
                       lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
                       nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK

    def simulator(idArray: MArray[ID] = MArray(),
                  nbrMap: MMap[ID, Set[ID]] = MMap(),
                  localSensors: PartialFunction[CNAME, PartialFunction[ID, Any]] = Map.empty,
                  nbrSensors: PartialFunction[CNAME, PartialFunction[(ID,ID), Any]] = Map.empty
                 ): NETWORK

    def gridLike(gsettings: GridSettings,
                 rng: Double,
                 lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap(),
                 seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK
  }

  class BasicSimulatorFactory extends SimulatorFactory with StandardSpatialSensorNames {
    protected val lId = linearID

    def basicSimulator(
                        idArray: MArray[ID] = MArray(),
                        nbrMap: MMap[ID, Set[ID]] = MMap(),
                        lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
                        nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap()
                        ): NETWORK =
      NetworkSimulator(idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.defaultRepr(_), SIM_SEED, RANDOM_SENSOR_SEED)

    def simulator(idArray: MArray[ID] = MArray(),
                  nbrMap: MMap[ID, Set[ID]] = MMap(),
                  localSensors: PartialFunction[CNAME, PartialFunction[ID, Any]] = Map.empty,
                  nbrSensors: PartialFunction[CNAME, PartialFunction[(ID,ID), Any]] = Map.empty
                 ): NETWORK =
      new NetworkSimulator(SIM_SEED, RANDOM_SENSOR_SEED, idArray, localSensors, nbrSensors, nbrMap, NetworkSimulator.defaultRepr(_))

    def gridLike(gsettings: GridSettings,
                 rng: Double,
                 lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap(),
                 seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK = {
      val GridSettings(rows, cols, stepx, stepy, tolerance, offsx, offsy, mapPos) = gsettings
      val configRandom = new Random(seeds.configSeed)

      def rnd(): Double = configRandom.nextDouble() * 2 * tolerance - tolerance

      def dist(a: (Double, Double), b: (Double, Double)): Double =
        Math.sqrt((a._1 - b._1) * (a._1 - b._1) + (a._2 - b._2) * (a._2 - b._2))

      import Array._
      val grid = ofDim[(Double, Double)](rows, cols)
      for (i <- 0 until rows;
           j <- 0 until cols) {
        grid(i)(j) = mapPos(i, j, offsx + i.toDouble * stepx + rnd(), offsy + j.toDouble * stepy + rnd())
      }
      val idArray = MArray() ++= (0 until rows * cols) map lId.fromNum

      val nbrMap = MMap() ++= idArray.map(lId.toNum(_)).map { i =>
        (lId.fromNum(i), idArray.filter { j =>
          dist(grid(i % rows)(i / rows), grid(lId.toNum(j) % rows)(lId.toNum(j) / rows)) < rng && j != lId.fromNum(i)
        }.toSet + lId.fromNum(i))
      }
      def nbsExportsInGridFor(i: ID) = MMap[ID, Any]((nbrMap(i) + i).toList.map(
        j => (j -> dist(grid(lId.toNum(i).toInt % rows)(lId.toNum(i) / rows), grid(lId.toNum(j) % rows)(lId.toNum(j) / rows)))
      ): _*)
      nsnsMap += (NBR_RANGE -> MMap(idArray.toList.map(i => i -> nbsExportsInGridFor(i)): _*))

      NetworkSimulator(
        idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.gridRepr(rows)(_),
        seeds.simulationSeed, seeds.randomSensorSeed)
    }

    /*
    override def random(n: Int,
                        fromx: Double,
                        tox: Double,
                        fromy: Double,
                        toy: Double,
                        eps: Double,
                        rng: Double,
                        seed: Long,
                        lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                        nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): Network with SimulatorOps = {
      val rand = new Random(seed)
      def r(min:Double, max:Double) = min+rand.nextInt(max.toInt-min.toInt)
      val devs = for(ids <- (0 until n);
                     x = r(fromx,tox);
                     y = r(fromy,toy)) yield(ids,x,y)
      val ids = MArray(devs.map(_._1):_*)
      import math._
      val nbrMap = devs.map(d => (d._1, devs.filter(other =>
        sqrt(pow(d._2 - other._2, 2) + pow(d._3 - other._3, 2)) < rng
      ).map(_._1))).toMap
      new NetworkSimulator(ids, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.GridRepr(n))
    }
    */
  }

  class NetworkSimulator(val simulationSeed: Long = 0L,
                         val randomSensorSeed: Long = 0L,
                         val idArray: MArray[ID] = MArray(),
                         val localSensors: PartialFunction[CNAME, PartialFunction[ID, Any]] = Map.empty,
                         val nbrSensors: PartialFunction[CNAME, PartialFunction[(ID,ID), Any]] = Map.empty,
                         val nbrMap: MMap[ID, Set[ID]] = MMap(),
                         val toStr: NetworkSimulator => String = NetworkSimulator.defaultRepr
                         ) extends Network with SimulatorOps with SimpleSource {
    self: NETWORK =>
    override type O = SimulationObserver[ID,CNAME]
    protected val eMap: MMap[ID,EXPORT] = MMap()
    protected var lastRound: Map[ID,Instant] = Map()
    protected val simulationRandom = new Random(simulationSeed)
    protected val randomSensor = new Random(randomSensorSeed)

    val lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap()
    val nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap()

    // *****************
    // Network interface
    // *****************

    val ids = idArray.toSet

    def neighbourhood(id: ID): Set[ID] = nbrMap.getOrElse(id, Set())

    def inputNeighbours(id: ID): Set[ID] = context(id).exports().map(_._1).toSet[ID]

    def sensorState(filter: (CNAME,ID) => Boolean = (s,n) => true): Map[CNAME, collection.Map[ID,Any]] =
      lsnsMap.toMap

    def neighbouringSensorState(filter: (CNAME,ID,ID) => Boolean = (s,n,nbr) => true): collection.Map[CNAME, collection.Map[ID, collection.Map[ID, Any]]] =
      nsnsMap.toMap

    def localSensor[A](name: CNAME)(id: ID): A =
      lsnsMap.get(name).flatMap(_.get(id)).getOrElse(localSensors(name)(id).asInstanceOf[A]).asInstanceOf[A]

    def nbrSensor[A](name: CNAME)(id: ID)(idn: ID): A =
      nsnsMap.get(name).flatMap(_.get(id)).flatMap(_.get(idn)).getOrElse(nbrSensors(name)(id, idn).asInstanceOf[A]).asInstanceOf[A]

    def export(id: ID): Option[EXPORT] = eMap.get(id)

    def exports(): IMap[ID, Option[EXPORT]] = ids.map(id => (id, export(id))).toMap

    protected var sensors = Map[CNAME,Any]()

    // **********************
    // SimulatorOps interface
    // **********************

    def getSensor(name: CNAME): Option[Any] = sensors.get(name)

    def addSensor[A](name: CNAME, value: A) {
      this.sensors += name -> value
      lsnsMap += (name -> MMap(idArray.map((_: ID) -> value).toSeq: _*))
    }

    def chgSensorValue[A](name: CNAME, ids: Set[ID], value: A) = ids.foreach { id => lsnsMap(name) += id -> value }

    override def clearExports(): Unit = eMap.clear()

    private def getExports(id: ID): Iterable[(ID,EXPORT)] =
      (neighbourhood(id) + id).intersect(eMap.keySet).toList.map{x => { x -> eMap(x)}}

    class SimulatorContextImpl(id: ID)
      extends ContextImpl(
        selfId = id,
        exports = getExports(id),
        localSensor = IMap(),
        nbrSensor = IMap())
    with StandardTemporalSensorNames with StandardPlatformSensorNames {
      import NetworkSimulator.Optionable
      def localSensorRetrieve[T](lsns: CNAME, id: ID): Option[T] =
        lsnsMap.get(lsns).flatMap(_.get(id)).orElse(Some(localSensors(lsns)(id))).map (_.asInstanceOf[T] )

      def nbrSensorRetrieve[T](nsns: CNAME, id: ID, nbr: ID): Option[T] =
        nsnsMap.get(nsns).flatMap(_.get(id)).flatMap(_.get(nbr)).orElse(Some(nbrSensors(nsns)(id, nbr))).map(_.asInstanceOf[T])

      override def sense[T](lsns: CNAME): Option[T] = lsns match {
        case _ if !lsnsMap.get(lsns).flatMap(_.get(id)).isEmpty => lsnsMap(lsns).get(id).map(_.asInstanceOf[T])
        case LSNS_RANDOM => randomSensor.some[T]
        case LSNS_TIME => Instant.now().some[T]
        case LSNS_TIMESTAMP => System.currentTimeMillis().some[T]
        case LSNS_DELTA_TIME => FiniteDuration(
          lastRound.get(id).map(t => ChronoUnit.NANOS.between(t, Instant.now())).getOrElse(0L),
          TimeUnit.NANOSECONDS).some[T]
        case _ => this.localSensorRetrieve(lsns, id)
      }

      override def nbrSense[T](nsns: CNAME)(nbr: ID): Option[T] = nsns match {
        case NBR_LAG => lastRound.get(nbr).map(nbrLast =>
          FiniteDuration(ChronoUnit.NANOS.between(nbrLast, Instant.now()), TimeUnit.NANOSECONDS)
        ).getOrElse(FiniteDuration(0L, TimeUnit.NANOSECONDS)).some[T]
        case NBR_DELAY => lastRound.get(nbr).map(nbrLast =>
          FiniteDuration(
            ChronoUnit.NANOS.between(
              nbrLast.plusNanos(
                lastRound.get(id).map(t => ChronoUnit.NANOS.between(t, Instant.now())).getOrElse(0L)),
              Instant.now()),
          TimeUnit.NANOSECONDS
        )).getOrElse(FiniteDuration(0L, TimeUnit.NANOSECONDS)).some[T]
        case _ => nbrSensorRetrieve(nsns, id, nbr)
      }
    }

    def context(id: ID): CONTEXT = new SimulatorContextImpl(id)

    def exec(node: EXECUTION, exp: => Any, id: ID): Unit = {
      val c = context(id)
      eMap += (id -> node.round(c, exp))
      lastRound += id -> Instant.now()
    }

    /**
     * @param node The local execution node
     * @param exp The expression to be run
     * @param size The number of executions to be performed
     * @param action An optional action launched after each execution
     */
    def execMany(node: EXECUTION, exp: => Any, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Seq[ID] = {
      var executedNodes = Seq[ID]()
      for (i <- 0 until size) {
        val nextIdToRun = idArray(simulationRandom.nextInt(idArray.size))
        executedNodes :+= nextIdToRun
        exec(node, exp, nextIdToRun)
        action(this, i)
      }
      executedNodes
    }

    def executeMany(node: EXECUTION, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Seq[ID] = {
      execMany(node, node.main(), size, action)
    }

    def exec(ap: CONTEXT=>EXPORT): (ID,EXPORT) = {
      val idToRun = idArray(simulationRandom.nextInt(idArray.size))
      val c = context(idToRun)
      val (_,exp) = idToRun -> ap(c)
      eMap += idToRun -> exp
      idToRun -> exp
    }

    override def execInOrderAndReturn(node: EXECUTION,
                                      exp: => Any,
                                      firingSeq: Seq[ID]): Network with SimulatorOps = {
      firingSeq.foreach(id => exec(node, exp, id))
      this
    }

    override def toString: String = toStr(this)
  }
  object NetworkSimulator extends Serializable {
    def apply(_idArray: MArray[ID] = MArray(),
              _nbrsMap: MMap[ID, Set[ID]] = MMap(),
              _lsnsMap: MMap[CNAME, MMap[ID, Any]] = MMap(),
              _nsnsMap: MMap[CNAME, MMap[ID, MMap[ID, Any]]] = MMap(),
              _toStr: NetworkSimulator => String = NetworkSimulator.defaultRepr,
              _simulationSeed: Long,
              _randomSensorSeed: Long
             ): NETWORK = {
      new NetworkSimulator (
        _simulationSeed,
        _randomSensorSeed,
        _idArray,
        Map.empty : PartialFunction[CNAME,PartialFunction[ID,Any]],
        Map.empty : PartialFunction[CNAME,PartialFunction[(ID,ID),Any]],
        _nbrsMap,
        _toStr
      ) {
        this.lsnsMap ++= _lsnsMap
        this.nsnsMap ++= _nsnsMap
      }
    }

    implicit class Optionable[T](obj: T) {
      def some[U]: Option[U] = Option[U](obj.asInstanceOf[U])
    }

    def defaultRepr(net: NetworkSimulator): String = {
      net.idArray.map {
        i => net.export(i).map { e => e.root().toString }.getOrElse("_")
      }.mkString("", "\t", "")
    }

    def gridRepr(numCols: Int)(net: NetworkSimulator): String = {
      net.idArray.map {
        i => net.export(i).map { e => e.root().toString }.getOrElse("_")
      }.zipWithIndex
        .map(z => (if (z._2 % numCols == 0) "\n" else "") + z._1)
        .mkString("", "\t", "")
    }
  }
}
