package io.asyncdb
package nio
package mysql

trait Codecs {
  def nBytesReader(n: Int): Reader[Array[Byte]]             = ???
  implicit val init1Reader: Reader[Int1]                    = ???
  implicit val delimitedBytesReader: Reader[DelimitedBytes] = ???
}

object Codecs extends Codecs
