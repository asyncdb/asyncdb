package io.asyncdb.nio.mysql.packet.server

import io.asyncdb.nio.mysql.{CharsetMap, Codec, Reader, Unsafe}

case class OK(
  affectedRows: Long,
  lastInsertId: Long,
  statusFlags: Int,
  warnings: Int,
  message: String
)

object OK {
  implicit val oKReader: Reader[OK] = {
    Codec.reader { buf =>
      println("start read ok message")
      val ar = Unsafe.readLengthLong(buf)
      val id = Unsafe.readLengthLong(buf)
      val sf = Unsafe.readInt2(buf)
      val ws = Unsafe.readInt2(buf)
      val ms = Unsafe.readNullEnded(buf)
      OK(
        ar.value,
        id.value,
        sf.value,
        ws.value,
        new String(ms, CharsetMap.of(CharsetMap.Utf8_bin))
      )
    }
  }
}
