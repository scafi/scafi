/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibo.scafi.space.optimization

import scala.{math => scalaMath}
/**
 * Convenience methods to handle Flink's [[org.apache.flink.ml.math.Matrix]] and [[Vector]]
 * abstraction.
 */
package object helper {
  implicit class RichMatrix(matrix: Matrix) extends Iterable[(Int, Int, Double)] {

    override def iterator: Iterator[(Int, Int, Double)] = {
      new Iterator[(Int, Int, Double)] {
        var index = 0

        override def hasNext: Boolean = {
          index < matrix.numRows * matrix.numCols
        }

        override def next(): (Int, Int, Double) = {
          val row = index % matrix.numRows
          val column = index / matrix.numRows

          index += 1

          (row, column, matrix(row, column))
        }
      }
    }

    def valueIterator: Iterator[Double] = {
      val it = iterator

      new Iterator[Double] {
        override def hasNext: Boolean = it.hasNext

        override def next(): Double = it.next._3
      }
    }

  }

  implicit class RichVector(vector: MultiVector) extends Iterable[(Int, Double)] {

    override def iterator: Iterator[(Int, Double)] = {
      new Iterator[(Int, Double)] {
        var index = 0

        override def hasNext: Boolean = {
          index < vector.size
        }

        override def next(): (Int, Double) = {
          val resultIndex = index

          index += 1

          (resultIndex, vector(resultIndex))
        }
      }
    }
    def ++ (v2 : MultiVector): MultiVector = DenseMultiVector(this.map{ x => v2.apply(x._1) + x._2}.toArray)


    def -- (v2 : MultiVector): MultiVector = DenseMultiVector(this.map{ x => v2.apply(x._1) - x._2}.toArray)

    def * (a : Double) : MultiVector = {
      DenseMultiVector(this.map{ x => x._2 * a}.toArray)
    }

    def distance(v : MultiVector) : Double = {
      scalaMath.sqrt(this.map{x => scalaMath.pow(x._2 - v(x._1),2) } sum)
    }


    def valueIterator: Iterator[Double] = {
      val it = iterator

      new Iterator[Double] {
        override def hasNext: Boolean = it.hasNext

        override def next(): Double = it.next._2
      }
    }
  }

  /** Stores the vector values in a dense array
    *
    * @param vector Subtype of [[MultiVector]]
    * @return Array containing the vector values
    */
  def vector2Array(vector: MultiVector): Array[Double] = {
    vector match {
      case dense: DenseMultiVector => dense.data.clone

      case sparse: SparseMultiVector => {
        val result = new Array[Double](sparse.size)

        for ((index, value) <- sparse) {
          result(index) = value
        }

        result
      }
    }
  }
}
