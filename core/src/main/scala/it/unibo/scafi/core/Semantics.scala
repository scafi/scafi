package it.unibo.scafi.core

import scala.collection.immutable.Stack
import scala.util.control.Exception._

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
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

  trait ContextOps { self: CONTEXT =>
    def readSlot[A](i: ID, p: Path): Option[A]
  }

  trait Slot extends Serializable
  sealed case class Nbr[A](index: Int) extends Slot
  sealed case class Rep[A](index: Int) extends Slot
  sealed case class FunCall[A](index: Int, funId: Any) extends Slot

  trait Path {
    def push(slot: Slot): Path
    def pull(): Path
    def matches(path: Path): Boolean
    def isRoot: Boolean

    def /(slot: Slot) = push(slot)
  }

  trait ExportOps { self: EXPORT =>
    def put[A](path: Path, value: A): A
    def get[A](path: Path): Option[A]
  }

  trait Factory {
    def emptyPath(): Path
    def emptyExport(): EXPORT
    def path(path: Slot*): Path
    def export(exps: (Path,Any)*): EXPORT
  }

  implicit val factory: Factory

  trait AggregateProgramSpecification { constructs: Constructs =>
    type MainResult
    def main(): MainResult
  }

  /**
   * It implements the whole operational semantics.
   */
  trait ExecutionTemplate extends (CONTEXT => EXPORT) with Constructs with AggregateProgramSpecification {
    self:Constructs =>

    import ExecutionTemplate._

    @transient private var ctx: CONTEXT = _
    @transient private var exp: EXPORT = _
    @transient private var status: Status = _

    def apply(c: CONTEXT): EXPORT = {
      round(c,main())
    }

    def round(c: CONTEXT, e: =>Any = main()): EXPORT = {
        ctx = c
        exp = factory.emptyExport
        status = Status()
        exp.put(factory.emptyPath, e)
        this.exp
    }

    def mid(): ID = ctx.selfId

    def foldingNeighbour(): ID = status.nbrStack.head

    def rep[A](init: A)(fun: (A) => A): A = {
      nest(Rep[A](status.index)) {
        val in = ctx.readSlot(ctx.selfId, status.path).getOrElse(init)
        fun(in)
      }
    }

    def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      try {
        val v = aligned()
        val res = v.map { i =>
          handling(classOf[OutOfDomainException]) by (_ => init) apply {
            frozen { evalInContext(i, expr) }
          }
        }
        res.fold(init)(aggr)
      } finally {
        status = status.incIndex()
      }
    }

    // Works only if aligned yields self as last element..
    // Why? Because nest performs 'exp.put(status.path, expr)'
    // So the export must be overridden by the current device (at last).
    def nbr[A](expr: => A): A = {
      ensure(status.isFolding, "nbr should be nested into fold")
      nest(Nbr[A](status.index)) {
        if (foldingNeighbour == ctx.selfId){
          expr
        } else {
          ctx.readSlot[A](foldingNeighbour, status.path)
             .getOrElse(throw new OutOfDomainException(ctx.selfId, foldingNeighbour, status.path))
        }
      }
    }

    def aggregate[T](f: => T): T = {
      var funId = Thread.currentThread().getStackTrace()(3)

      nest(FunCall[T](status.index, funId)) { f }
    }

    def sense[A](name: LSNS): A = ctx.sense[A](name).getOrElse(throw new SensorUnknownException(ctx.selfId, name))

    def nbrvar[A](name: NSNS): A = {
      ctx.nbrSense(name)(foldingNeighbour).getOrElse{
        throw new NbrSensorUnknownException(ctx.selfId, name, foldingNeighbour)
      }
    }

    private[this] def nest[A](slot: Slot)(expr: => A): A = {
      try {
        status = status.push().nest(slot)  // prepare nested call
        exp.put(status.path, expr)         // function return value is result of expr
      } finally {
        status = status.pop().incIndex();  // do not forget to restore the status
      }
    }

    private[this] def evalInContext[A](id: ID, expr: => A): A = {
      try {
        status = status.foldInto(id)
        expr
      } finally {
        status = status.foldOut()
      }
    }

    private[this] def frozen[A](expr: => A): A = {
      try {
        status = status.push()
        expr
      } finally {
        status = status.pop()
      }
    }

    private[this] def aligned(): List[ID] =
      ctx.exports
        .filter(p => p._1 != ctx.selfId && (status.path.isRoot || p._2.get(status.path).isDefined))
        .map(_._1)
        .toList
        .++(List(ctx.selfId))
  }

  private[scafi] object ExecutionTemplate extends Serializable {

    trait Status extends Serializable {
      val path: Path
      val index: Int
      val nbrStack: List[ID]

      def isFolding: Boolean
      def foldInto(id: ID): Status
      def foldOut(): Status
      def nest(s: Slot): Status
      def incIndex(): Status
      def push(): Status
      def pop(): Status
    }

    private case class StatusImpl(
        path: Path = factory.emptyPath(),
        index: Int = 0,
        nbrStack: List[ID] = List(),
        stack: List[(Path, Int, List[ID])] = List()) extends Status {

      def isFolding: Boolean = !nbrStack.isEmpty
      def foldInto(id: ID): Status = StatusImpl(path, index, id :: nbrStack, stack)
      def foldOut(): Status = StatusImpl(path, index, nbrStack.tail, stack)
      def push(): Status = StatusImpl(path, index, nbrStack, (path, index, nbrStack) :: stack)
      def pop(): Status = stack match {
        case (p, i, n) :: s => StatusImpl(p, i, n, s)
        case _           => throw new Exception()
      }
      def nest(s: Slot): Status = StatusImpl(path.push(s), 0, nbrStack, stack)
      def incIndex(): Status = StatusImpl(path, index + 1, nbrStack, stack)
    }

    object Status {
      def apply(): Status = StatusImpl()
    }

    def ensure(b: => Boolean, s: String): Unit = {
      b match {
        case false => throw new Exception(s)
        case _     =>
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

}
