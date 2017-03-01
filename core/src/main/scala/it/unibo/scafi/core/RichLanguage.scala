package it.unibo.scafi.core

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 * This trait defines a component that extends LanguageStructure and requires to be "attached" to Core
 * It defines a trait with additional language mechanisms, in the form of certain builtins, and Ordering implicits
 *
 */

trait RichLanguage extends Language { self: Core =>

  trait Builtins { this: Constructs =>
    import Builtins._

    def branch[A](cond: => Boolean)(th: => A)(el: => A): A =
      mux(cond)(() => aggregate{ th })(() => aggregate{ el })()

    def mux[A](cond: Boolean)(th: A)(el: A): A = if (cond) th else el

    def minHood[A](expr: => A)(implicit of: OrderingFoldable[A]): A = foldhood[A](of.top)((x, y) => of.min(x, y)){expr}
    def maxHood[A](expr: => A)(implicit of: OrderingFoldable[A]): A = foldhood[A](of.bottom)((x, y) => of.max(x, y)){expr}

    def foldhoodPlus[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      foldhood(init)(aggr)(mux(mid()==nbr(mid())){init}{expr})

    def minHoodPlus[A](expr: => A)(implicit of: OrderingFoldable[A]): A = foldhoodPlus[A](of.top)((x, y) => of.min(x, y)){expr}
    def maxHoodPlus[A](expr: => A)(implicit of: OrderingFoldable[A]): A = foldhoodPlus[A](of.bottom)((x, y) => of.max(x, y)){expr}
  }

  object Builtins extends Serializable {

    trait OrderingFoldable[A] {
      def top: A
      def bottom: A
      def compare(a: A, b: A): Int
      def same(a: A, b: A): Boolean = compare(a, b) == 0
      def min(a: A, b: A): A = if (compare(a, b) <= 0) a else b
      def max(a: A, b: A): A = if (compare(a, b) > 0) a else b
    }

    object OrderingFoldable extends Serializable {

      @transient implicit val of_i = new OrderingFoldable[Int] {
        def top: Int = Int.MaxValue
        def bottom: Int = Int.MinValue
        def compare(a: Int, b: Int): Int = a.compareTo(b)
      }

      @transient implicit val of_d = new OrderingFoldable[Double] {
        def top: Double = Double.MaxValue
        def bottom: Double = Double.MinValue
        def compare(a: Double, b: Double): Int = (a-b).signum
      }

      @transient implicit val of_s = new OrderingFoldable[String] {
        def top: String = "Z"
        def bottom: String = "A"
        def compare(a: String, b: String): Int = if (a > b) 1 else if (b < a) -1 else 0
      }

      @transient implicit def of_ft[T : OrderingFoldable]: OrderingFoldable[()=>T] =
        new OrderingFoldable[()=>T] {
          val oft = implicitly[OrderingFoldable[T]]
          def top: ()=>T = ()=> oft.top
          def bottom: ()=>T = () => oft.bottom
          def compare(a: ()=>T, b: ()=>T): Int = oft.compare(a(),b())
        }

      @transient implicit def of_tpl[T1, T2](implicit of1: OrderingFoldable[T1], of2: OrderingFoldable[T2]): OrderingFoldable[(T1, T2)] =
        new OrderingFoldable[(T1, T2)] {
          def top: (T1, T2) = (of1.top, of2.top)
          def bottom: (T1, T2) = (of1.bottom, of2.bottom)
          def compare(a: (T1, T2), b: (T1, T2)): Int =
            if (of1.compare(a._1, b._1) == 0) of2.compare(a._2, b._2) else of1.compare(a._1, b._1)
        }

      /*
      object Specific {

      implicit def oft1[T1, T2](implicit of1: OrderingFoldable[T1]): OrderingFoldable[Tuple2[T1, T2]] =
        new OrderingFoldable[Tuple2[T1, T2]] {
          var t: T2 = _
          def top: Tuple2[T1, T2] = (of1.top, t)
          def bottom: Tuple2[T1, T2] = (of1.bottom, t)
          def compare(a: Tuple2[T1, T2], b: Tuple2[T1, T2]): Int = of1.compare(a._1, b._1)
        }

      }*/
    }

  }

}
