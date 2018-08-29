package io.asyncdb
package nio
package mysql

import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal

case class Int1(value: Byte)                  extends AnyVal
case class Int2(value: Int)                   extends AnyVal
case class Int3(value: Int)                   extends AnyVal
case class IntLE(value: Int)                  extends AnyVal
case class LenencInt(value: Int)              extends AnyVal
case class DelimitedBytes(value: Array[Byte]) extends AnyVal
case class DelimitedUTF8String(value: String) extends AnyVal

object sized {

  trait Size10 extends Any {
    def size = 10
  }
  trait Size20 extends Any {
    def size = 20
  }

  case class Bytes10(bytes: Array[Byte]) extends AnyVal with Size10
  case class Bytes20(bytes: Array[Byte]) extends AnyVal with Size20

}
