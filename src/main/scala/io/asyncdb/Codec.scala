package io.asyncdb

trait BasicEncoders {
  val byteEncoder: Encoder[Byte]
  val intEncoder: Encoder[Int]
  val longEncoder: Encoder[Long]
  val bytesEncoder: Encoder[Array[Byte]]
}
