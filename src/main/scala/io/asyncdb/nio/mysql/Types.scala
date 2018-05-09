package io.asyncdb
package nio
package mysql

import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal

case class Int1(val value: Byte)                  extends AnyVal
case class Int2(val value: Int)                   extends AnyVal
case class Int3(val value: Int)                   extends AnyVal
case class IntLE(val value: Int)                  extends AnyVal
case class LenencInt(val value: Int)              extends AnyVal
case class DelimitedBytes(val value: Array[Byte]) extends AnyVal
case class DelimitedUTF8String(val value: String) extends AnyVal
