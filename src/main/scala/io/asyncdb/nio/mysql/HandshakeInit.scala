package io.asyncdb
package nio
package mysql

import java.nio.charset.{Charset, StandardCharsets}
import BasicCodecs._

/**
 * https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10
 */
case class ExtraHandshakeData(
  characterSet: Int1,
  statusFlags: Int2,
  capabilityFlagUpper: Int2,
  authPluginDataLen: Int1,
  reserved: Array[Byte],
  authPluginDataPart2: Array[Byte]
)

case class HandshakeInit(
  protocol: Int1,
  version: String,
  connectionId: Int,
  authPluginData: Array[Byte],
  charset: Charset,
  cap: Int
)

object HandshakeInit {
  implicit object HandshakeInitReader extends Reader[HandshakeInit] {

    private def readExtra(capFlagLow: Int2, buf: BufView) = {

      def readPart2(len: Int1) = {
        val isSecure = Cap(len.value.toInt).has(Cap.SecureConnection)
        if (isSecure) {
          readNBytes(buf, math.max(13, len.value.toInt - 8))
        } else {
          decodeBuf[NullDelimitedBytes](buf).map(_.value)
        }
      }

      if (buf.hasRemaining()) {
        for {
          cs   <- decodeBuf[Int1](buf)
          sf   <- decodeBuf[Int2](buf)
          cfu  <- decodeBuf[Int2](buf)
          len  <- decodeBuf[Int1](buf)
          re   <- readNBytes(buf, 10)
          adp2 <- readPart2(len)
        } yield ExtraHandshakeData(cs, sf, cfu, len, re, adp2)

      } else {
        Left(new Exception("Unsupported server version"))
      }
    }

    def read(buf: BufView) = {
      for {
        p    <- decodeBuf[Int1](buf)
        v    <- decodeBuf[NullDelimitedBytes](buf)
        cid  <- decodeBuf[IntLE](buf)
        apd1 <- readNBytes(buf, 8)
        f    <- decodeBuf[Int1](buf)
        cfl  <- decodeBuf[Int2](buf)
        ex   <- readExtra(cfl, buf)
      } yield {
        val authData = apd1 ++ ex.authPluginDataPart2
        val cap      = (cfl.value & 0xff) | (ex.capabilityFlagUpper.value & 0xff000000)
        val cs       = CharsetMap.of((ex.characterSet.value & 0x00ff).toShort)
        val version  = new String(v.value, cs)
        HandshakeInit(
          p,
          version,
          cid.value,
          authData,
          StandardCharsets.UTF_8,
          cap)
      }
    }
  }
}
