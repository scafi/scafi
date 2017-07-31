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

      @transient implicit val of_i = new Bounded[Int] {
        def top: Int = Int.MaxValue
        def bottom: Int = Int.MinValue
        def compare(a: Int, b: Int): Int = a.compareTo(b)
      }

      @transient implicit val of_d = new Bounded[Double] {
        def top: Double = Double.MaxValue
        def bottom: Double = Double.MinValue
        def compare(a: Double, b: Double): Int = (a-b).signum
      }

      @transient implicit val of_s = new Bounded[String] {
        def top: String = "Z"
        def bottom: String = "A"
        def compare(a: String, b: String): Int = if (a > b) 1 else if (b < a) -1 else 0
      }

      @transient implicit def funcBounded[T : Bounded]: Bounded[()=>T] =
        new Bounded[()=>T] {
          val oft = implicitly[Bounded[T]]
          def top: ()=>T = ()=> oft.top
          def bottom: ()=>T = () => oft.bottom
          def compare(a: ()=>T, b: ()=>T): Int = oft.compare(a(),b())
        }

      @transient implicit def tupleBounded[T1, T2](implicit of1: Bounded[T1], of2: Bounded[T2]): Bounded[(T1, T2)] =
        new Bounded[(T1, T2)] {
          def top: (T1, T2) = (of1.top, of2.top)
          def bottom: (T1, T2) = (of1.bottom, of2.bottom)
          def compare(a: (T1, T2), b: (T1, T2)): Int =
            if (of1.compare(a._1, b._1) == 0) of2.compare(a._2, b._2) else of1.compare(a._1, b._1)
        }

      @transient implicit def tupleOnFirstBounded[T1, T2](implicit of1: Bounded[T1], of2: Defaultable[T2]): Bounded[(T1, T2)] =
        new Bounded[(T1, T2)] {
          def top: (T1, T2) = (of1.top, of2.default)
          def bottom: (T1, T2) = (of1.bottom, of2.default)
          def compare(a: (T1, T2), b: (T1, T2)): Int = of1.compare(a._1, b._1)
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
