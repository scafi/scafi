package it.unibo.scafi.incarnations

import it.unibo.scafi.core.{RichLanguage, Engine, Core}

/**
 * @author Roberto Casadei
 *
 */

trait Incarnation extends Core with Engine with RichLanguage {
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
    override def rep[A](init: A)(fun: (A) => A): A = engine.rep(init)(fun)
  }
}