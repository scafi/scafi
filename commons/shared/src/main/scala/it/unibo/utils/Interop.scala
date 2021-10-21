package it.unibo.utils

trait Interop[T] {
  def toString(data: T): String
  def fromString(s: String): T
}

trait LinearizableTo[T,N] {
  def toNum(v: T): N
  def fromNum(n: N): T
}

trait Linearizable[T] extends LinearizableTo[T,Int]
