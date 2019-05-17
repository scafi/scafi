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

  override type EXPORT = Export with ExportOps with Serializable
  override type CONTEXT = Context with ContextOps
  override type FACTORY = Factory

  override implicit val factory = new EngineFactory

  class ExportImpl() extends Export with ExportOps with Serializable { self: EXPORT =>
    private val map = MMap[Path,Any]()

    def getMap[A]: Map[Path, A] = map.map { case (k,v) => k -> v.asInstanceOf[A] }.toMap
    def put[A](path: Path, value: A) : A = { map += (path -> value); value }
    def get[A](path: Path): Option[A] = map get(path) map (_.asInstanceOf[A])
    def root[A](): A = get[A](factory.emptyPath()).get
    def paths : Map[Path,Any] = Map(map.toSeq:_*)

    override def toString: String = map.toString

    override def getAll: scala.collection.Map[Path, Any] = map
  }

  class PathImpl(val path: List[Slot]) extends Path with Equals with Serializable {
    def push(s: Slot): Path = new PathImpl(s :: path)
    def pull(): Path = path match {
      case s :: p => new PathImpl(p)
      case _ => throw new Exception()
    }

    override def isRoot: Boolean = path.isEmpty

    override def toString(): String = "P:/" + path.reverse.mkString("/")

    def matches(p: Path): Boolean = this == p

    def canEqual(other: Any): Boolean = {
      other.isInstanceOf[Engine.this.PathImpl]
    }

    override def equals(other: Any): Boolean = {
      other match {
        case that: Engine.this.PathImpl => that.canEqual(PathImpl.this) && path == that.path
        case _ => false
      }
    }

    override def hashCode(): Int = path.hashCode

    override def head: Slot = path.head
  }

  abstract class BaseContextImpl(val selfId: ID,
                                 _exports: Iterable[(ID, EXPORT)])
    extends Context with ContextOps with Serializable { self: CONTEXT =>

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
      val localSensor: GMap[LSNS,Any],
      val nbrSensor: GMap[NSNS,GMap[ID,Any]])
    extends BaseContextImpl(selfId, exports) { self: CONTEXT =>

    override def toString(): String = s"C[\n\tI:$selfId,\n\tE:$exports,\n\tS1:$localSensor,\n\tS2:$nbrSensor\n]"

    override def sense[T](lsns: LSNS): Option[T] = localSensor.get(lsns).map(_.asInstanceOf[T])

    override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] = nbrSensor.get(nsns).flatMap(_.get(nbr)).map(_.asInstanceOf[T])
  }

  class EngineFactory extends Factory with Serializable { self: FACTORY =>
    def /(): Path = emptyPath()
    def /(s: Slot): Path = path(s)
    def emptyPath(): Path = new PathImpl(List())
    def emptyExport(): EXPORT = new ExportImpl
    def path(slots: Slot*): Path = new PathImpl(List(slots:_*).reverse)
    def export(exps: (Path,Any)*): EXPORT = {
      val exp = new ExportImpl()
      exps.foreach { case (p,v) => exp.put(p,v) }
      exp
    }
    def context(selfId: ID, exports: Map[ID,EXPORT], lsens: Map[LSNS,Any] = Map(), nbsens: Map[NSNS,Map[ID,Any]] = Map()): CONTEXT =
      new ContextImpl(selfId, exports, lsens, nbsens)
  }

  implicit val linearID: Linearizable[ID]
  implicit val interopID: Interop[ID]
  implicit val interopLSNS: Interop[LSNS]
  implicit val interopNSNS: Interop[NSNS]
}
