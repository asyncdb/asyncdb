package io.asyncdb
package nio
package mysql

import shapeless._
import scala.util._

trait Reader[A] {
  def read(buf: Vector[Packet]): Either[Throwable, A]
}

trait Writer[A] {
  def write(a: A, packets: Vector[Packet]): Vector[Packet]
}

object Codec {
  def read[A](buf: Vector[Packet])(
    implicit reader: Reader[A]): Either[Throwable, A] = reader.read(buf)

  def write[A](a: A)(implicit writer: Writer[A]) =
    writer.write(a, Vector.empty[Packet])

  implicit val hnilReader: Reader[HNil] = new Reader[HNil] {
    def read(buf: Vector[Packet]) = {
      Right(HNil)
    }
  }
}
