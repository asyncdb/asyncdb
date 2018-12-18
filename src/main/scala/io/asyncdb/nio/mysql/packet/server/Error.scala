package io.asyncdb
package nio
package mysql
package packet
package server

import io.asyncdb.util.Hex

case class Error(
  errorCode: Int,
  sqlState: String,
  errorMessage: String
)

object Error {
  implicit val errorReader: Reader[Error] = {
    Codec.reader { buf =>
      Unsafe.readInt1(buf)
      val code  = Unsafe.readInt2(buf)
      val state = Unsafe.readN(buf, 6)
      val msg   = Unsafe.readNullEnded(buf)
      Error(
        code.value,
        new String(state, CharsetMap.of(CharsetMap.Utf8_bin)),
        new String(msg, CharsetMap.of(CharsetMap.Utf8_bin))
      )
    }
  }
}
