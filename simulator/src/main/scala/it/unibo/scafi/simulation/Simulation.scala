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

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.platform.SimulationPlatform

import scala.collection.immutable.{Map => IMap}
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

    def addSensor[A](name: LSNS, value: A)

    def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A)

    def clearExports()

    def exec(node: EXECUTION, exp: => Any, id: ID)

    def execMany(node: EXECUTION, exp: => Any, size: Int, action: (Network, Int) => Unit)

    def executeMany(node: EXECUTION, size: Int, action: (Network, Int) => Unit)

    def execInOrderAndReturn(node: EXECUTION, exp: => Any, firingSeq: Seq[ID]): NETWORK

    def exec(ap: CONTEXT=>EXPORT): (ID,EXPORT)
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
                       lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                       nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK

    def gridLike(gsettings: GridSettings,
                 rng: Double,
                 lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap(),
                 seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK
  }

  class BasicSimulatorFactory extends SimulatorFactory {
    protected val lId = linearID

    def basicSimulator(
                        idArray: MArray[ID] = MArray(),
                        nbrMap: MMap[ID, Set[ID]] = MMap(),
                        lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                        nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()
                        ): NETWORK =
      new NetworkSimulator(idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.defaultRepr, SIM_SEED, RANDOM_SENSOR_SEED)

    def gridLike(gsettings: GridSettings,
                 rng: Double,
                 lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap(),
                 seeds: Seeds = Seeds(CONFIG_SEED, SIM_SEED, RANDOM_SENSOR_SEED)): NETWORK = {
      val GridSettings(rows, cols, stepx, stepy, tolerance, offsx, offsy) = gsettings
      val configRandom = new Random(seeds.configSeed)

      def rnd(): Double = configRandom.nextDouble() * 2 * tolerance - tolerance

      def dist(a: (Double, Double), b: (Double, Double)): Double =
        Math.sqrt((a._1 - b._1) * (a._1 - b._1) + (a._2 - b._2) * (a._2 - b._2))

      import Array._
      val grid = ofDim[(Double, Double)](rows, cols)
      for (i <- 0 until rows;
           j <- 0 until cols) {
        grid(i)(j) = (offsx + i.toDouble * stepx + rnd(), offsy + j.toDouble * stepy + rnd())
      }
      val idArray = MArray() ++= (0 until rows * cols) map lId.fromNum

      val nbrMap = MMap() ++= idArray.map(lId.toNum(_)).map { i =>
        (lId.fromNum(i), idArray.filter { j =>
          dist(grid(i % rows)(i / rows), grid(lId.toNum(j) % rows)(lId.toNum(j) / rows)) < rng && j != lId.fromNum(i)
        }.toSet)
      }
      def nbsExportsInGridFor(i: ID) = MMap[ID, Any]((nbrMap(i) + i).toList.map(
        j => (j -> dist(grid(lId.toNum(i).toInt % rows)(lId.toNum(i) / rows), grid(lId.toNum(j) % rows)(lId.toNum(j) / rows)))
      ): _*)
      nsnsMap += (NBR_RANGE_NAME -> MMap(idArray.toList.map(i => i -> nbsExportsInGridFor(i)): _*))

      new NetworkSimulator(
        idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.gridRepr(rows),
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

  object NetworkSimulator extends Serializable {
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

  class NetworkSimulator(val idArray: MArray[ID] = MArray(),
                         val nbrMap: MMap[ID, Set[ID]] = MMap(),
                         val lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                         val nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap(),
                         val toStr: NetworkSimulator => String = NetworkSimulator.defaultRepr,
                         val simulationSeed: Long,
                         val randomSensorSeed: Long
                         ) extends Network with SimulatorOps {
    self: NETWORK =>

    protected val eMap: MMap[ID, EXPORT] = MMap()
    protected var lastRound: Map[ID,LocalDateTime] = Map()

    protected val simulationRandom = new Random(simulationSeed)
    protected val randomSensor = new Random(randomSensorSeed)

    // *****************
    // Network interface
    // *****************

    val ids = idArray.toSet

    def neighbourhood(id: ID): Set[ID] = nbrMap.getOrElse(id, Set())

    def localSensor[A](name: LSNS)(id: ID): A = lsnsMap(name)(id).asInstanceOf[A]

    def nbrSensor[A](name: NSNS)(id: ID)(idn: ID): A = nsnsMap(name)(id)(idn).asInstanceOf[A]

    def export(id: ID): Option[EXPORT] = eMap.get(id)

    def exports(): IMap[ID, Option[EXPORT]] = ids.map(id => (id, export(id))).toMap

    protected var sensors = Map[LSNS,Any]()

    // **********************
    // SimulatorOps interface
    // **********************

    def getSensor(name: LSNS): Option[Any] = sensors.get(name)

    def addSensor[A](name: LSNS, value: A) {
      this.sensors += name -> value
      lsnsMap += (name -> MMap(idArray.map((_: ID) -> value).toSeq: _*))
    }

    def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A) {
      ids.foreach { id => lsnsMap(name) += id -> value }
    }

    override def clearExports(): Unit = eMap.clear()

    private def getExports(id: ID): Iterable[(ID,EXPORT)] = {
       val nhood = neighbourhood(id) + id
       eMap.filter(kv => nhood.contains(kv._1))
    }

    class SimulatorContextImpl(id: ID)
      extends ContextImpl(
        selfId = id,
        exports = getExports(id),
        localSensor = IMap(),
        nbrSensor = IMap()){

      import NetworkSimulator.Optionable

      def localSensorRetrieve[T](lsns: LSNS, id: ID): Option[T] =
        lsnsMap(lsns).get(id).map (_.asInstanceOf[T] )

      def nbrSensorRetrieve[T](nsns: NSNS, id: ID, nbr: ID): Option[T] =
        nsnsMap(nsns)(id).get(nbr).map(_.asInstanceOf[T])

      override def sense[T](lsns: LSNS): Option[T] = lsns match {
        case LSNS_RANDOM => randomSensor.some[T]
        case LSNS_TIME => LocalDateTime.now().some[T]
        case LSNS_TIMESTAMP => System.currentTimeMillis().some[T]
        case LSNS_DELTA_TIME => FiniteDuration(
          lastRound.get(id).map(t => ChronoUnit.NANOS.between(t, LocalDateTime.now())).getOrElse(0L),
          TimeUnit.NANOSECONDS).some[T]
        case _ => localSensorRetrieve(lsns, id)
      }

      override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] = nsns match {
        case NBR_LAG => lastRound.get(nbr).map(nbrLast =>
          FiniteDuration(ChronoUnit.NANOS.between(nbrLast, LocalDateTime.now()), TimeUnit.NANOSECONDS)
        ).getOrElse(FiniteDuration(0L, TimeUnit.NANOSECONDS)).some[T]
        case NBR_DELAY => lastRound.get(nbr).map(nbrLast =>
          FiniteDuration(
            ChronoUnit.NANOS.between(
              nbrLast.plusNanos(
                lastRound.get(id).map(t => ChronoUnit.NANOS.between(t, LocalDateTime.now())).getOrElse(0L)),
              LocalDateTime.now()),
          TimeUnit.NANOSECONDS
        )).getOrElse(FiniteDuration(0L, TimeUnit.NANOSECONDS)).some[T]
        case _ => nbrSensorRetrieve(nsns, id, nbr)
      }
    }

    def context(id: ID): CONTEXT = new SimulatorContextImpl(id)

    def exec(node: EXECUTION, exp: => Any, id: ID): Unit = {
      val c = context(id)
      eMap += (id -> node.round(c, exp))
      lastRound += id -> LocalDateTime.now()
    }

    /**
     * @param node The local execution node
     * @param exp The expression to be run
     * @param size The number of executions to be performed
     * @param action An optional action launched after each execution
     */
    def execMany(node: EXECUTION, exp: => Any, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Unit = {
      for (i <- 0 until size) {
        val nextIdToRun = idArray(simulationRandom.nextInt(idArray.size))
        exec(node, exp, nextIdToRun)
        action(this, i)
      }
    }

    def executeMany(node: EXECUTION, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Unit = {
      execMany(node, node.main(), size, action)
    }

    def exec(ap: CONTEXT=>EXPORT): (ID,EXPORT) = {
      val idToRun = idArray(simulationRandom.nextInt(idArray.size))
      val c = context(idToRun)
      val (_,exp) = idToRun -> ap(c)
      lastRound += idToRun -> LocalDateTime.now()
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
}
