package io.asyncdb

trait BasicReads {
  val byteRead: Read[Byte]
  val intRead: Read[Int]
  val longRead: Read[Long]
  val byteaRead: Read[Array[Byte]]
}

trait BasicWrites {
  val byteWrites: Write[Byte]
  val intWrites: Write[Int]
  val longWrite: Write[Long]
  val byteaWrites: Write[Array[Byte]]
}
