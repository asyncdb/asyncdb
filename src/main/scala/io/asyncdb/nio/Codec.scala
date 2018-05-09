package io.asyncdb
package nio

import scala.util._

trait Reader[A] {
  def read(buf: ByteVector): Either[Throwable, A]
  def flatMap[B](f: A => Reader[B]): Reader[B] = {
    val self = this
    new Reader[B] {
      def read(buf: ByteVector): Either[Throwable, B] = {
        self.read(buf).flatMap { a =>
          f(a).read(buf)
        }
      }
    }
  }
  def map[B](f: A => B): Reader[B] = {
    val self = this
    new Reader[B] {
      def read(buf: ByteVector) = self.read(buf).map(f)
    }
  }
}

trait Writer[A] {
  def write(a: A, buf: Buf): Unit
}

object Codec {
  def read[A](buf: ByteVector)(
    implicit reader: Reader[A]): Either[Throwable, A] = reader.read(buf)
}
