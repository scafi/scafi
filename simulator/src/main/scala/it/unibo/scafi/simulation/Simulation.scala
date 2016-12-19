package it.unibo.scafi.simulation

import it.unibo.scafi.core.{Core, Engine}
import it.unibo.scafi.platform.{Platform}
import scala.collection.{Map => GMap}
import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{Map => MMap, ArrayBuffer => MArray}
import scala.util.Random

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 *         This trait defines a component that extends a Platform and
 *         requires to be "attached" to an Engine.
 *         It defines a trait with a simulator skeleton along with two settings
 *         for it, created by a factory.
 *
 */

trait Simulation extends Platform { self: Platform.PlatformDependency =>

  override type NETWORK = Network with SimulatorOps

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

  trait SimulatorFactory {
    def basicSimulator(idArray: MArray[ID] = MArray(),
                       nbrMap: MMap[ID, Set[ID]] = MMap(),
                       lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                       nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK

    def gridLike(n: Int,
                 m: Int,
                 stepx: Double = 1,
                 stepy: Double = 1,
                 eps: Double = 0.0,
                 rng: Double,
                 lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap())
                (implicit nbrRangeName: NSNS, lId: Linearizable[ID]): NETWORK

    /*
    def random(n: Int,
               fromx: Double = 0,
               tox: Double = 100,
               fromy: Double = 0,
               toy: Double = 100,
               eps: Double = 0.0,
               rng: Double = 1,
               seed: Long = 0,
               lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
               nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()): NETWORK
               */
  }

  def simulatorFactory = new SimulatorFactory {
    def basicSimulator(
                        idArray: MArray[ID] = MArray(),
                        nbrMap: MMap[ID, Set[ID]] = MMap(),
                        lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                        nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap()
                        ): NETWORK =
      new NetworkSimulator(idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.DefaultRepr)

    def gridLike(n: Int,
                 m: Int,
                 stepx: Double,
                 stepy: Double,
                 eps: Double,
                 rng: Double,
                 lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                 nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap())
                (implicit nbrRangeName: NSNS, lId: Linearizable[ID]): NETWORK = {
      def rnd(): Double = Math.random() * 2 * eps - eps

      def dist(a: (Double, Double), b: (Double, Double)): Double =
        Math.sqrt((a._1 - b._1) * (a._1 - b._1) + (a._2 - b._2) * (a._2 - b._2))

      import Array._
      val grid = ofDim[(Double, Double)](n, m)
      for (i <- 0 until n;
           j <- 0 until m) {
        grid(i)(j) = (i.toDouble * stepx + rnd(), j.toDouble * stepy + rnd())
      }
      val idArray = MArray() ++= (0 until n * m) map lId.fromNum

      val nbrMap = MMap() ++= idArray.map(lId.toNum(_)).map { i => (lId.fromNum(i), idArray.filter { j => dist(grid(i % n)(i / n), grid(lId.toNum(j) % n)(lId.toNum(j) / n)) < rng && j != lId.fromNum(i) }.toSet) }
      def nbsExportsInGridFor(i: ID) = MMap[ID, Any](nbrMap(i).+(i).toList.map(
        j => (j -> dist(grid(lId.toNum(i).toInt % n)(lId.toNum(i) / n), grid(lId.toNum(j) % n)(lId.toNum(j) / n)))
      ): _*)
      nsnsMap += (nbrRangeName -> MMap(idArray.toList.map(i => i -> nbsExportsInGridFor(i)): _*))

      new NetworkSimulator(idArray, nbrMap, lsnsMap, nsnsMap, NetworkSimulator.GridRepr(n))
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
    def DefaultRepr(net: NetworkSimulator): String = {
      net.idArray.map {
        i => net.export(i).map { e => e.root().toString }.getOrElse("_")
      }.mkString("", "\t", "")
    }

    def GridRepr(numCols: Int)(net: NetworkSimulator): String = {
      net.idArray.map {
        i => net.export(i).map { e => e.root().toString }.getOrElse("_")
      }
        .zipWithIndex
        .map(z => (if (z._2 % numCols == 0) "\n" else "") + z._1)
        .mkString("", "\t", "")
    }
  }

  class NetworkSimulator(val idArray: MArray[ID] = MArray(),
                         val nbrMap: MMap[ID, Set[ID]] = MMap(),
                         val lsnsMap: MMap[LSNS, MMap[ID, Any]] = MMap(),
                         val nsnsMap: MMap[NSNS, MMap[ID, MMap[ID, Any]]] = MMap(),
                         val toStr: NetworkSimulator => String = NetworkSimulator.DefaultRepr
                         ) extends Network with SimulatorOps {
    self: NETWORK =>

    protected val eMap: MMap[ID, EXPORT] = MMap()

    // *****************
    // Network interface
    // *****************

    val ids = idArray.toSet

    def neighbourhood(id: ID): Set[ID] = nbrMap.getOrElse(id, Set())

    def localSensor[A](name: LSNS)(id: ID): A = lsnsMap(name)(id).asInstanceOf[A]

    def nbrSensor[A](name: NSNS)(id: ID)(idn: ID): A = nsnsMap(name)(id)(idn).asInstanceOf[A]

    def export(id: ID): Option[EXPORT] = eMap.get(id)

    def exports(): IMap[ID, Option[EXPORT]] = ids.map(id => (id, export(id))).toMap

    // **********************
    // SimulatorOps interface
    // **********************

    def addSensor[A](name: LSNS, value: A) {
      lsnsMap += (name -> MMap(idArray.map((_: ID) -> value).toSeq: _*))
    }

    def chgSensorValue[A](name: LSNS, ids: Set[ID], value: A) {
      ids.foreach { id => lsnsMap(name) += id -> value }
    }

    override def clearExports(): Unit = eMap.clear()

    def context(id: ID): CONTEXT = {
      val nhood = neighbourhood(id)+id

      new ContextImpl(
        selfId = id,
        exports = eMap.filter(kv => nhood.contains(kv._1)),
      localSensor = IMap(),
      nbrSensor = IMap()
      ) {
        override def sense[T](lsns: LSNS): Option[T] =
          lsnsMap(lsns).get(this.selfId).map(_.asInstanceOf[T])
        override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] =
          nsnsMap(nsns)(this.selfId).get(nbr).map(_.asInstanceOf[T])
      }
    }

    def exec(node: EXECUTION, exp: => Any, id: ID): Unit = {
      val c = context(id)
      eMap += (id -> node.round(c, exp))
    }

    /**
     * @param node The local execution node
     * @param exp The expression to be run
     * @param size The number of executions to be performed
     * @param action An optional action launched after each execution
     */
    def execMany(node: EXECUTION, exp: => Any, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Unit = {
      for (i <- 0 until size) {
        val nextIdToRun = idArray(scala.util.Random.nextInt(idArray.size))
        exec(node, exp, nextIdToRun)
        action(this, i)
      }
    }

    def executeMany(node: EXECUTION, size: Int, action: (Network, Int) => Unit = (n, i) => {}): Unit = {
      execMany(node, node.main(), size, action)
    }

    def exec(ap: CONTEXT=>EXPORT): (ID,EXPORT) = {
      val idToRun = idArray(scala.util.Random.nextInt(idArray.size))
      val c = context(idToRun)
      val (nextIdToRun,exp) = idToRun -> ap(c)
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
