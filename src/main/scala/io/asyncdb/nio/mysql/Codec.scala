package io.asyncdb
package nio
package mysql

import cats.data.NonEmptyList
import scala.util._
import shapeless._

trait Reader[A] {
  def read(buf: NonEmptyList[Packet]): Either[Throwable, A]
}

trait Writer[A] {
  def write(a: A): NonEmptyList[Packet]
}

object Reader {

  implicit val int1Reader: Reader[Int1] = Codec.reader[Int1] { buf =>
    Int1(buf.get)
  }

  implicit val int3Reader: Reader[Int3] = Codec.reader[Int3] { buf =>
    val bytes = buf.take(3)
    val v = (bytes(0).toInt & 0xff) | ((bytes(1).toInt & 0xff) << 8) | ((bytes(
      2).toInt & 0xff) << 16)
    Int3(v)
  }

  //little endian ordered int
  implicit val intLEReader: Reader[IntLE] = Codec.reader[IntLE] { buf =>
    val bytes = buf.take(4)
    val v = (bytes(0).toInt & 0xff) | ((bytes(1).toInt & 0xff) << 8) | ((bytes(
      2).toInt & 0xff) << 16) | (bytes(4).toInt & 0xff) << 24
    IntLE(v)
  }

  implicit val hnilReader: Reader[HNil] = new Reader[HNil] {
    def read(packets: NonEmptyList[Packet]) = Right(HNil)
  }

  implicit def hconsReader[H, T <: HList](
    implicit hr: Reader[H],
    tr: Reader[T]): Reader[H :: T] = new Reader[H :: T] {
    def read(packets: NonEmptyList[Packet]) = {
      hr.read(packets).flatMap { h =>
        tr.read(packets).map { t =>
          h :: t
        }
      }
    }
  }

  implicit def caseClassReader[A, L <: HList](
    implicit gen: Generic.Aux[A, L],
    lr: Reader[L]
  ): Reader[A] = new Reader[A] {
    def read(packets: NonEmptyList[Packet]) = {
      lr.read(packets).map(gen.from)
    }
  }

}

object Codec {

  def reader[A](f: BufferReader => A): Reader[A] =
    new Reader[A] {
      def read(packets: NonEmptyList[Packet]) = {
        val buf: BufferReader = packets.map { p =>
          BufferReader(p.payload)
        }.reduceLeft(_ ++ _)
        try {
          Right(f(buf))
        } catch {
          case e: Throwable => Left(e)
        }
      }
    }

  def read[A](buf: NonEmptyList[Packet])(
    implicit reader: Reader[A]): Either[Throwable, A] = reader.read(buf)

  def write[A](a: A)(implicit writer: Writer[A]) =
    writer.write(a)

}
