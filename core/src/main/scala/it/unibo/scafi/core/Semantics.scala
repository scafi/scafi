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

import it.unibo.scafi.PlatformDependentConstants

import scala.util.control.Exception._

/**
 * This trait defines a component that extends Core and Language
 * It starts concretising the framework by implementing the key element of field-calculus semantics, namely:
 * - An export is a map from paths to values, and a value is a list of slots
 * - An Execution template implementing the whole operational semantics
 * - A basic Factory
 * - Additional ops to Context and Export, realised by family polymorphism
 *
 * This is still abstract in that we do not dictate how Context and Export are implemented and optimised internally
 */

trait Semantics extends Core with Language {

  override type CONTEXT <: Context with ContextOps
  override type EXPORT <: Export with ExportOps
  override type EXECUTION <: ExecutionTemplate
  type FACTORY <: Factory

  implicit val factory: Factory

  trait Slot extends Serializable{
    def ->(v: Any): (Path,Any) = (factory.path(this), v)
    def /(s: Slot): Path = factory.path(this, s)
  }
  sealed case class Nbr[A](index: Int) extends Slot
  sealed case class Rep[A](index: Int) extends Slot
  sealed case class FunCall[A](index: Int, funId: Any) extends Slot
  sealed case class FoldHood[A](index: Int) extends Slot

  trait Path {
    def push(slot: Slot): Path
    def pull(): Path
    def matches(path: Path): Boolean
    def isRoot: Boolean

    def /(slot: Slot): Path = push(slot)
  }

  trait ExportOps { self: EXPORT =>
    def put[A](path: Path, value: A): A
    def get[A](path: Path): Option[A]
  }

  trait ContextOps { self: CONTEXT =>
    def readSlot[A](i: ID, p: Path): Option[A]
  }

  trait Factory {
    def emptyPath(): Path
    def emptyExport(): EXPORT
    def path(slots: Slot*): Path
    def export(exps: (Path,Any)*): EXPORT
  }

  trait ProgramSchema {
    type MainResult
    def main(): MainResult
  }

  trait AggregateProgramSchema extends ProgramSchema {
    self: Constructs =>
  }

  /**
   * It implements the whole operational semantics.
   */
  trait ExecutionTemplate extends (CONTEXT => EXPORT) with ConstructsSemantics with ProgramSchema {

    var vm: RoundVM = _

    def apply(c: CONTEXT): EXPORT = {
      round(c,main())
    }

    def round(c: CONTEXT, e: =>Any = main()): EXPORT = {
      vm = new RoundVMImpl(c)
      val result = e
      vm.registerRoot(result)
      vm.export
    }
  }

  trait ConstructsSemantics extends Constructs {
    def vm: RoundVM

    override def mid(): ID = vm.self

    override def rep[A](init: =>A)(fun: (A) => A): A = {
      vm.nest(Rep[A](vm.index))(true) {
        vm.locally {
          fun(vm.previousRoundVal.getOrElse(init))
        }
      }
    }

    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      vm.nest(FoldHood[A](vm.index))(true) {
        val nbrField = vm.alignedNeighbours
          .map(id => vm.foldedEval(expr)(id).getOrElse(vm.locally { init }))
        vm.isolate { nbrField.fold(vm.locally { init })((x,y) => aggr(x,y) ) }
      }
    }

    override def nbr[A](expr: => A): A =
      vm.nest(Nbr[A](vm.index))(vm.neighbour.map(_==vm.self).getOrElse(false)) {
        vm.neighbour match {
          case Some(nbr) if (nbr != vm.self) => vm.neighbourVal
          case _  => expr
        }
      }

    override def aggregate[T](f: => T): T =
      vm.nest(FunCall[T](vm.index, vm.elicitAggregateFunctionTag()))(true) {
        f
      }

    def sense[A](name: LSNS): A = vm.localSense(name)

    def nbrvar[A](name: NSNS): A = vm.neighbourSense(name)

  }

  trait RoundVM {

    def export: EXPORT

    def registerRoot(v: Any): Unit

    def self: ID

    def neighbour: Option[ID]

    def index: Int

    def previousRoundVal[A]: Option[A]

    def neighbourVal[A]: A

    def foldedEval[A](expr: => A)(id: ID): Option[A]

    def localSense[A](name: LSNS): A

    def neighbourSense[A](name: NSNS): A

    def nest[A](slot: Slot)(write: Boolean)(expr: => A): A

    def locally[A](a: => A): A

    def alignedNeighbours(): List[ID]

    def elicitAggregateFunctionTag(): Any

    def isolate[A](expr: => A): A
  }

  class RoundVMImpl(val context: CONTEXT) extends RoundVM {
    import RoundVMImpl.{ensure, Status, StatusImpl}

    var export: EXPORT = factory.emptyExport
    var status: Status = Status()
    var isolated = false // When true, neighbours are scoped out

    override def registerRoot(v: Any): Unit = export.put(factory.emptyPath, v)

    override def self: ID = context.selfId

    override def neighbour: Option[ID] = status.neighbour

    override def index: Int = status.index

    override def previousRoundVal[A]: Option[A] = context.readSlot[A](self, status.path)

    override def neighbourVal[A]: A = context
      .readSlot[A](neighbour.get, status.path)
      .getOrElse(throw new OutOfDomainException(context.selfId, neighbour.get, status.path))

    override def foldedEval[A](expr: =>A)(id: ID): Option[A] =
      handling(classOf[OutOfDomainException]) by (_ => None) apply {
        try {
          status = status.push()
          status = status.foldInto(Some(id))
          Some(expr)
        } finally {
          status = status.pop()
        }
      }

    override def localSense[A](name: LSNS): A = context
      .sense[A](name)
      .getOrElse(throw new SensorUnknownException(self, name))

    override def neighbourSense[A](name: NSNS): A = {
      ensure(neighbour.isDefined, "Neighbouring sensor must be queried in a nbr-dependent context.")
      context.nbrSense(name)(neighbour.get).getOrElse(throw new NbrSensorUnknownException(self, name, neighbour.get))
    }

    override def nest[A](slot: Slot)(write: Boolean)(expr: => A): A = {
      try {
        status = status.push().nest(slot)  // prepare nested call
        if (write) export.get(status.path).getOrElse(export.put(status.path, expr)) else expr  // function return value is result of expr
      } finally {
        status = status.pop().incIndex();  // do not forget to restore the status
      }
    }

    override def locally[A](a: =>A): A = {
      val currentNeighbour = neighbour
      try{
        status = status.foldOut()
        a
      } finally {
        status = status.foldInto(currentNeighbour)
      }
    }

    override def alignedNeighbours(): List[ID] =
      if(isolated) {
        List()
      } else {
        self ::
          context.exports
            .filter(_._1 != self)
            .filter(p => status.path.isRoot || p._2.get(status.path).isDefined)
            .map(_._1)
            .toList
      }

    override def elicitAggregateFunctionTag():Any =
      Thread.currentThread().getStackTrace()(PlatformDependentConstants.StackTracePosition)

    override def isolate[A](expr: => A): A = {
      val wasIsolated = this.isolated
      try {
        this.isolated = true
        expr
      } finally {
        this.isolated = wasIsolated
      }
    }
  }

  object RoundVMImpl {
    def ensure(b: => Boolean, s: String): Unit = {
      b match {
        case false => throw new Exception(s)
        case _     =>
      }
    }

    trait Status extends Serializable {
      val path: Path
      val index: Int
      val neighbour: Option[ID]

      def isFolding: Boolean
      def foldInto(id: Option[ID]): Status
      def foldOut(): Status
      def nest(s: Slot): Status
      def incIndex(): Status
      def push(): Status
      def pop(): Status
    }

    private case class StatusImpl(
                                   path: Path = factory.emptyPath(),
                                   index: Int = 0,
                                   neighbour: Option[ID] = None,
                                   stack: List[(Path, Int, Option[ID])] = List()) extends Status {

      def isFolding: Boolean = neighbour.isDefined
      def foldInto(id: Option[ID]): Status = StatusImpl(path, index, id, stack)
      def foldOut(): Status = StatusImpl(path, index, None, stack)
      def push(): Status = StatusImpl(path, index, neighbour, (path, index, neighbour) :: stack)
      def pop(): Status = stack match {
        case (p, i, n) :: s => StatusImpl(p, i, n, s)
        case _           => throw new Exception()
      }
      def nest(s: Slot): Status = StatusImpl(path.push(s), 0, neighbour, stack)
      def incIndex(): Status = StatusImpl(path, index + 1, neighbour, stack)
    }

    object Status {
      def apply(): Status = StatusImpl()
    }
  }

  case class OutOfDomainException(selfId: ID, nbr: ID, path: Path) extends Exception() {
    override def toString: String = s"OutOfDomainException: $selfId , $nbr, $path"
  }

  case class SensorUnknownException(selfId: ID, name: LSNS) extends Exception() {
    override def toString: String = s"SensorUnknownException: $selfId , $name"
  }

  case class NbrSensorUnknownException(selfId: ID, name: NSNS, nbr: ID) extends Exception() {
    override def toString: String = s"NbrSensorUnknownException: $selfId , $name, $nbr"
  }

}
