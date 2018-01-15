package io.asyncdb
package nio
package mysql

import scala.util._

trait Reader[A] {
  def read(buf: Buf): Either[Throwable, A]
}

trait Writer[A] {
  def write(a: A, buf: Buf): Unit
}

trait Readers {}
