package it.unibo.scafi.incarnations

import it.unibo.scafi.core.{Core, Engine, RichLanguage}
import it.unibo.scafi.platform.SpaceTimeAwarePlatform
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
 * @author Roberto Casadei
 *
 */

trait Incarnation extends Core
  with Engine
  with RichLanguage
  with SpaceTimeAwarePlatform
  with BasicSpatialAbstraction
  with TimeAbstraction {

  trait AggregateProgramSpec extends AggregateProgramSpecification with Constructs with Builtins

  trait AggregateInterpreter extends ExecutionTemplate with Constructs with Builtins with Serializable {
    type MainResult = Any
  }

  trait AggregateProgram extends AggregateInterpreter

  class BasicAggregateInterpreter extends AggregateInterpreter {
    override def main() = ???
  }

  trait StandardSensors { self: AggregateProgram =>
    def nbrDelay(): FiniteDuration = nbrvar[FiniteDuration](NBR_DELAY)
    def nbrLag(): FiniteDuration = nbrvar[FiniteDuration](NBR_LAG)
    def nbrRange(): D = nbrvar[D](NBR_RANGE_NAME)
    def nbrVector(): P = nbrvar[P](NBR_VECTOR)
    def currentPosition(): P = sense[P](LSNS_POSITION)
    def currentTime(): Time = sense[Time](LSNS_TIME)
    def deltaTime(): FiniteDuration = sense[FiniteDuration](LSNS_DELTA_TIME)
  }

  class ScafiDSL extends Constructs with Builtins {
    var engine: ExecutionTemplate = _

    def Program[T](et: Option[ExecutionTemplate] = None)(expr: =>T): CONTEXT=>EXPORT = {
      engine = et.getOrElse(new ExecutionTemplate {
        override def main() = expr
        override type MainResult = T
      })
      (ctx: CONTEXT) => engine(ctx)
    }

    override def mid(): ID = engine.mid()
    override def aggregate[A](f: => A): A = engine.aggregate(f)
    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = engine.foldhood(init)(aggr)(expr)
    override def nbrvar[A](name: NSNS): A = engine.nbrvar(name)
    override def nbr[A](expr: => A): A = engine.nbr(expr)
    override def sense[A](name: LSNS): A = engine.sense(name)
    override def rep[A](init: =>A)(fun: (A) => A): A = engine.rep(init)(fun)
  }
}