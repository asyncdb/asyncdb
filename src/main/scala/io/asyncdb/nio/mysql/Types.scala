package io.asyncdb
package nio
package mysql

import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal
import shapeless._
import shapeless.nat._
import shapeless.syntax.sized._

case class Int1(value: Byte)                  extends AnyVal
case class Int2(value: Int)                   extends AnyVal
case class Int3(value: Int)                   extends AnyVal
case class IntLE(value: Int)                  extends AnyVal
case class LenencInt(value: Int)              extends AnyVal
case class DelimitedBytes(value: Array[Byte]) extends AnyVal
case class DelimitedUTF8String(value: String) extends AnyVal

object fixed {
  type Bytes[N <: Nat] = Sized[Array[Byte], N]
}
