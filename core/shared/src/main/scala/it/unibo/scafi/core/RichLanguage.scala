/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.core

/**
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

    def minHoodLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhood[A](default)((x, y) => if(poglb.equiv(x, y)) poglb.gle(x,y) else if(poglb.lt(x,y)) x else y){expr}

    def minHoodPlusLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhoodPlus[A](default)((x, y) => if(poglb.equiv(x, y)) poglb.gle(x,y) else if(poglb.lt(x,y)) x else y){expr}

    def minHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.top)((x, y) => of.min(x, y)){expr}
    def maxHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.bottom)((x, y) => of.max(x, y)){expr}

    def foldhoodPlus[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      foldhood(init)(aggr)(mux(mid()==nbr(mid())){init}{expr})

    def minHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.top)((x, y) => of.min(x, y)){expr}
    def maxHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.bottom)((x, y) => of.max(x, y)){expr}
  }

  object Builtins extends Serializable {

    trait Ordered[A] {
      def compare(a: A, b: A): Int
      def same(a: A, b: A): Boolean = compare(a, b) == 0
      def min(a: A, b: A): A = if (compare(a, b) <= 0) a else b
      def max(a: A, b: A): A = if (compare(a, b) > 0) a else b
    }

    trait LowerBounded[A] extends Ordered[A] {
      def bottom: A
    }

    trait UpperBounded[A] extends Ordered[A] {
      def top: A
    }

    trait Bounded[A] extends LowerBounded[A] with UpperBounded[A] {
    }

    object Bounded extends Serializable {

      @transient implicit val of_i: Bounded[Int] = new Bounded[Int] {
        def top: Int = Int.MaxValue
        def bottom: Int = Int.MinValue
        def compare(a: Int, b: Int): Int = a.compareTo(b)
      }

      @transient implicit val of_d: Bounded[Double] = new Bounded[Double] {
        def top: Double = Double.PositiveInfinity
        def bottom: Double = Double.NegativeInfinity
        def compare(a: Double, b: Double): Int = (a-b).signum
      }

      @transient implicit val of_s: Bounded[String] = new Bounded[String] {
        def top: String = "Z"
        def bottom: String = "A"
        def compare(a: String, b: String): Int = if (a > b) 1 else if (b < a) -1 else 0
      }

      implicit def funcBounded[T : Bounded]: Bounded[()=>T] =
        new Bounded[()=>T] {
          val oft = implicitly[Bounded[T]]
          def top: ()=>T = ()=> oft.top
          def bottom: ()=>T = () => oft.bottom
          def compare(a: ()=>T, b: ()=>T): Int = oft.compare(a(),b())
        }

      implicit def tupleBounded[T1, T2](implicit of1: Bounded[T1], of2: Bounded[T2]): Bounded[(T1, T2)] =
        new Bounded[(T1, T2)] {
          def top: (T1, T2) = (of1.top, of2.top)
          def bottom: (T1, T2) = (of1.bottom, of2.bottom)
          def compare(a: (T1, T2), b: (T1, T2)): Int =
            if (of1.compare(a._1, b._1) == 0) of2.compare(a._2, b._2) else of1.compare(a._1, b._1)
        }

      implicit def tupleBounded4[T1, T2, T3, T4](implicit of1: Bounded[T1], of2: Bounded[T2], of3: Bounded[T3], of4: Bounded[T4]): Bounded[(T1, T2, T3, T4)] =
        new Bounded[(T1, T2, T3, T4)] {
          def top: (T1, T2, T3, T4) = (of1.top, of2.top, of3.top, of4.top)
          def bottom: (T1, T2, T3, T4) = (of1.bottom, of2.bottom, of3.top, of4.top)
          override def compare(a: (T1, T2, T3, T4), b: (T1, T2, T3, T4)): Int = {
            List(of1.compare(a._1,b._1), of2.compare(a._2,b._2), of3.compare(a._3, b._3), of4.compare(a._4, b._4))
              .filter(_ != 0)
              .lift(0)
              .getOrElse(0)
          }
        }

      implicit def tupleOnFirstBounded[T1, T2](implicit of1: Bounded[T1], of2: Defaultable[T2]): Bounded[(T1, T2)] =
        new Bounded[(T1, T2)] {
          def top: (T1, T2) = (of1.top, of2.default)
          def bottom: (T1, T2) = (of1.bottom, of2.default)
          def compare(a: (T1, T2), b: (T1, T2)): Int = of1.compare(a._1, b._1)
        }

      implicit def listBounded[T](implicit ev: Bounded[T]): Bounded[List[T]] =
        new Bounded[List[T]] {
          override def bottom: List[T] = List()
          override def top: List[T] = List()
          override def compare(a: List[T] , b: List[T] ): Int =
            a.zip(b)
              .dropWhile { case (x,y) => ev.compare(x,y)==0 }
              .headOption.map { case (x,y) => ev.compare(x,y) }
              .getOrElse(-1)
        }
    }

    trait PartialOrderingWithGLB[T] extends PartialOrdering[T] {
      def gle(x: T, y: T): T
    }

    object PartialOrderingWithGLB {
      implicit def fromBounded[T](implicit b: Bounded[T]): PartialOrderingWithGLB[T] = new PartialOrderingWithGLB[T] {
        override def gle(x: T, y: T): T = b.min(x,y)

        override def tryCompare(x: T, y: T): Option[Int] = Some(b.compare(x,y))

        override def lteq(x: T, y: T): Boolean = b.compare(x,y) <= 0
      }

      implicit val pogldouble: PartialOrderingWithGLB[Double] =
        new PartialOrderingWithGLB[Double] {
          override def gle(x: Double, y: Double): Double = Math.min(x,y)
          override def tryCompare(x: Double, y: Double): Option[Int] = None
          override def lteq(x: Double, y: Double): Boolean = x <= y
        }

      implicit def poglbTuple[T1, T2](implicit p1: PartialOrderingWithGLB[T1], p2: PartialOrderingWithGLB[T2]): PartialOrderingWithGLB[(T1, T2)] =
        new PartialOrderingWithGLB[(T1, T2)] {
          override def gle(x: (T1, T2), y: (T1, T2)): (T1, T2) = (p1.gle(x._1,y._1), p2.gle(x._2,y._2))
          override def tryCompare(x: (T1, T2), y: (T1, T2)): Option[Int] = None
          override def lteq(x: (T1, T2), y: (T1, T2)): Boolean = p1.lteq(x._1,y._1) && p2.lteq(x._2,y._2)
        }
    }

    trait Defaultable[T] {
      def default: T
    }

    object Defaultable {
      def apply[T](defaultVal: T): Defaultable[T] = new Defaultable[T] {
        def default: T = defaultVal
      }
    }

  }

}
