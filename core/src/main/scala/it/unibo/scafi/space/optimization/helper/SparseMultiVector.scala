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

package it.unibo.scafi.space.optimization.helper

import scala.util.Sorting

/** Sparse vector implementation storing the data in two arrays. One index contains the sorted
  * indices of the non-zero vector entries and the other the corresponding vector entries
  */
case class SparseMultiVector(size: Int, indices: Array[Int], data: Array[Double])
  extends MultiVector with Serializable {

  /** Updates the element at the given index with the provided value
    *
    * @param index Index whose value is updated.
    * @param value The value used to update the index.
    */
  override def update(index: Int, value: Double): Unit = {
    val resolvedIndex = locate(index)

    if (resolvedIndex < 0) {
      throw new IllegalArgumentException("Cannot update zero value of sparse vector at " +
        s"index $index")
    } else {
      data(resolvedIndex) = value
    }
  }

  /** Copies the vector instance
    *
    * @return Copy of the [[SparseMultiVector]] instance
    */
  override def copy: SparseMultiVector = {
    new SparseMultiVector(size, indices.clone, data.clone)
  }


  /** Magnitude of a vector
    *
    * @return The length of the vector
    */
  override def magnitude: Double = math.sqrt(data.map(x => x * x).sum)

  /** Element wise access function
    *
    * * @param index index of the accessed element
    * @return element with index
    */
  override def apply(index: Int): Double = {
    val resolvedIndex = locate(index)

    if(resolvedIndex < 0) {
      0
    } else {
      data(resolvedIndex)
    }
  }

  /** Converts the [[SparseMultiVector]] to a [[DenseMultiVector]]
    *
    * @return The DenseVector out of the SparseVector
    */
  def toDenseVector: DenseMultiVector = {
    val denseVector = DenseMultiVector.zeros(size)

    for(index <- 0 until size) {
      denseVector(index) = this(index)
    }

    denseVector
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case sv: SparseMultiVector if size == sv.size =>
        indices.sameElements(sv.indices) && data.sameElements(sv.data)
      case _ => false
    }
  }

  override def hashCode: Int = {
    val hashCodes = List(size.hashCode, java.util.Arrays.hashCode(indices),
      java.util.Arrays.hashCode(data))

    hashCodes.foldLeft(3){ (left, right) => left * 41 + right}
  }

  override def toString: String = {
    val entries = indices.zip(data).mkString(", ")
    "SparseVector(" + entries + ")"
  }

  private def locate(index: Int): Int = {
    require(0 <= index && index < size, index + " not in [0, " + size + ")")

    java.util.Arrays.binarySearch(indices, 0, indices.length, index)
  }
}

object SparseMultiVector {

  /** Constructs a sparse vector from a coordinate list (COO) representation where each entry
    * is stored as a tuple of (index, value).
    *
    * @param size The number of elements in the vector
    * @param entries The values in the vector
    * @return a new [[SparseMultiVector]]
    */
  def fromCOO(size: Int, entries: (Int, Double)*): SparseMultiVector = {
    fromCOO(size, entries)
  }

  /** Constructs a sparse vector from a coordinate list (COO) representation where each entry
    * is stored as a tuple of (index, value).
    *
    * @param size The number of elements in the vector
    * @param entries An iterator supplying the values in the vector
    * @return a new [[SparseMultiVector]]
    */
  def fromCOO(size: Int, entries: Iterable[(Int, Double)]): SparseMultiVector = {
    val entryArray = entries.toArray

    entryArray.foreach { case (index, _) =>
      require(0 <= index && index < size, index + " not in [0, " + size + ")")
    }

    val COOOrdering = new Ordering[(Int, Double)] {
      override def compare(x: (Int, Double), y: (Int, Double)): Int = {
        x._1 - y._1
      }
    }

    Sorting.quickSort(entryArray)(COOOrdering)

    // calculate size of the array
    val arraySize = entryArray.foldLeft((-1, 0)){ case ((lastIndex, numRows), (index, _)) =>
      if(lastIndex == index) {
        (lastIndex, numRows)
      } else {
        (index, numRows + 1)
      }
    }._2

    val indices = new Array[Int](arraySize)
    val data = new Array[Double](arraySize)

    val (index, value) = entryArray(0)

    indices(0) = index
    data(0) = value

    var i = 1
    var lastIndex = indices(0)
    var lastDataIndex = 0

    while(i < entryArray.length) {
      val (curIndex, curValue) = entryArray(i)

      if(curIndex == lastIndex) {
        data(lastDataIndex) += curValue
      } else {
        lastDataIndex += 1
        data(lastDataIndex) = curValue
        indices(lastDataIndex) = curIndex
        lastIndex = curIndex
      }

      i += 1
    }

    new SparseMultiVector(size, indices, data)
  }

  /** Convenience method to be able to instantiate a SparseVector with a single element. The Scala
    * type inference mechanism cannot infer that the second tuple value has to be of type Double
    * if only a single tuple is provided.
    *
    * @param size The number of elements in the vector
    * @param entry The value in the vector
    * @return a new [[SparseMultiVector]]
    */
  def fromCOO(size: Int, entry: (Int, Int)): SparseMultiVector = {
    fromCOO(size, (entry._1, entry._2.toDouble))
  }
}
