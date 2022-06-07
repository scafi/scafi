/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.core

/**
 * This trait defines a component that extends Semantics.
 * It defines an implementation of Context and Export (and Path),
 * with associated factories.
 */

import it.unibo.utils.{Interop, Linearizable}

import scala.collection.{Map => GMap}
import scala.collection.mutable.{Map => MMap}

trait Engine extends Semantics {

  override type EXPORT = Export with ExportOps
  override type CONTEXT = Context with ContextOps
  override type FACTORY = Factory

  override implicit val factory: EngineFactory = new EngineFactory

  class ExportImpl(private var map: Map[Path,Any] = Map.empty) extends Export with ExportOps with Equals { self: EXPORT =>
    override def put[A](path: Path, value: A) : A = { map += (path -> value); value }
    override def get[A](path: Path): Option[A] = map.get(path).map { case x:A @unchecked => x }
    override def root[A](): A = get[A](factory.emptyPath()).get
    override def paths : Map[Path,Any] = map

    override def equals(o: Any): Boolean = o match {
      case x: ExportOps => x.paths == map
      case _ => false
    }

    override def canEqual(that: Any): Boolean = that match { case _: Export => true; case _ => false }

    override def hashCode(): Int = map.hashCode()

    override def toString: String = map.toString
  }

  class PathImpl(val path: List[Slot]) extends Path with Equals {
    def push(s: Slot): Path = new PathImpl(s :: path)
    def pull(): Path = path match {
      case s :: p => new PathImpl(p)
      case _ => throw new Exception()
    }

    override def isRoot: Boolean = path.isEmpty

    override def toString(): String = "P:/" + path.reverse.mkString("/")

    def matches(p: Path): Boolean = this == p

    def canEqual(other: Any): Boolean = other match { case _: Path => true; case _ => false }

    override def equals(other: Any): Boolean = {
      other match {
        case that: Path => path == that.path
        case _ => false
      }
    }

    override def hashCode(): Int = path.hashCode

    override def head: Slot = path.head
  }

  abstract class BaseContextImpl(val selfId: ID,
                                 _exports: Iterable[(ID, EXPORT)])
    extends Context with ContextOps { self: CONTEXT =>

    private var exportsMap : Map[ID,EXPORT] = _exports.toMap
    def updateExport(id: ID, export:EXPORT): Unit = exportsMap += id -> export

    override def exports(): Iterable[(ID, EXPORT)] = exportsMap

    def readSlot[A](i: ID, p:Path): Option[A] = {
      exportsMap get(i) flatMap (_.get[A](p))
    }
  }

  class ContextImpl(
                     selfId: ID,
                     exports: Iterable[(ID,EXPORT)],
                     val localSensor: GMap[CNAME,Any],
                     val nbrSensor: GMap[CNAME,GMap[ID,Any]])
    extends BaseContextImpl(selfId, exports) { self: CONTEXT =>

    override def toString(): String =
      s"C[\n\tI:$selfId,\n\tE:$exports,\n\tS1:$localSensor,\n\tS2:$nbrSensor\n]"

    override def sense[T](localSensorName: CNAME): Option[T] =
      localSensor.get(localSensorName).map { case x:T @unchecked => x }

    override def nbrSense[T](nbrSensorName: CNAME)(nbr: ID): Option[T] =
      nbrSensor.get(nbrSensorName).flatMap(_.get(nbr)).map { case x:T @unchecked => x }
  }

  class EngineFactory extends Factory { self: FACTORY =>
    override def emptyPath(): Path = new PathImpl(List())
    override def emptyExport(): EXPORT = new ExportImpl
    override def path(slots: Slot*): Path = new PathImpl(List(slots:_*).reverse)
    override def export(exps: (Path,Any)*): EXPORT = {
      val exp = new ExportImpl()
      exps.foreach { case (p,v) => exp.put(p,v) }
      exp
    }
    override def context(selfId: ID,
                         exports: Map[ID,EXPORT],
                         lsens: Map[CNAME,Any] = Map.empty,
                         nbsens: Map[CNAME,Map[ID,Any]] = Map.empty): CONTEXT =
      new ContextImpl(selfId, exports, lsens, nbsens)
  }

  implicit val linearID: Linearizable[ID]
  implicit val interopID: Interop[ID]
  implicit val interopCNAME: Interop[CNAME]
}
