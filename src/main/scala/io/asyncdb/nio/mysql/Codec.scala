package io.asyncdb
package nio
package mysql

import scala.util._

trait Reader[A] {
  def read(buf: BufView): Either[Throwable, A]
  def flatMap[B](f: A => Reader[B]): Reader[B] = {
    val self = this
    new Reader[B] {
      def read(buf: BufView): Either[Throwable, B] = {
        self.read(buf).flatMap { a =>
          f(a).read(buf)
        }
      }
    }
  }
  def map[B](f: A => B): Reader[B] = {
    val self = this
    new Reader[B] {
      def read(buf: BufView) = self.read(buf).map(f)
    }
  }
}

trait Writer[A] {
  def write(a: A, buf: Buf): Unit
}
