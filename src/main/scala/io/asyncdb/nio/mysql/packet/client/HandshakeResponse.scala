package io.asyncdb
package nio
package mysql
package packet
package client

import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.MessageDigest

import cats.data.NonEmptyList
import io.asyncdb.util.Hex

case class HandshakeResponse(
  username: String,
  charset: Int,
  seed: Array[Byte],
  authenticationMethod: String,
  password: Option[String] = None,
  database: Option[String] = None
)

object HandshakeResponse {
  val Padding = Array.fill[Byte](23)(0)
  implicit val handshakeResponseWriter: Writer[HandshakeResponse] = {
    Codec.writer[HandshakeResponse] { hr =>
      val buf = BufferWriter.apply(1024)
      buf.order(ByteOrder.LITTLE_ENDIAN)
//      buf.writeInt(Cap.baseCap.mask)
//      println(s"the clientCapabilities is ${Cap.baseCap.mask}")
      buf.writeInt(696840)
      buf.writeInt(MaxPacketSize)
      buf.writeByte(hr.charset.toByte)
      buf.writeBytes(Padding)
      Unsafe.writeNullEndedString(
        buf,
        hr.username,
        CharsetMap.of(hr.charset.toShort)
      )
      hr.password match {
        case Some(p) =>
          val auth = Authentication.generateAuthentication(
            CharsetMap.of(hr.charset.toShort),
            p,
            hr.seed,
            hr.authenticationMethod
          )
          buf.writeByte(auth.length)
          buf.writeBytes(auth)
        case _ => buf.writeByte(0)
      }
      hr.database.foreach { db =>
        Unsafe.writeNullEndedString(buf, db, CharsetMap.of(hr.charset.toShort))
      }
      Unsafe.writeNullEndedString(
        buf,
        hr.authenticationMethod,
        CharsetMap.of(hr.charset.toShort)
      )
      NonEmptyList.of(Packet.toPacket(buf.value))
    }
  }
}

object Authentication {
  val Native = "mysql_native_password"
  val Old    = "mysql_old_password"

  def generateAuthentication(
    charset: Charset,
    password: String,
    seed: Array[Byte],
    authType: String
  ) = {
    authType match {
      case Native => scramble411(charset, password, seed)
      case _      => scramble411(charset, password, seed) // 老模式是否需要支持
    }
  }

  private def scramble411(
    charset: Charset,
    password: String,
    seed: Array[Byte]
  ) = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    val initialDigest = messageDigest.digest(password.getBytes(charset))
    messageDigest.reset()
    val finalDigest = messageDigest.digest(initialDigest)
    messageDigest.reset()
    messageDigest.update(seed)
    messageDigest.update(finalDigest)
    val result  = messageDigest.digest()
    var counter = 0
    println(s"r1:${Hex.fromBytes(result)}")
    while (counter < result.length) {
      result(counter) = (result(counter) ^ initialDigest(counter)).toByte
      counter += 1
    }
    println(s"r2:${Hex.fromBytes(result)}")
    result
  }
}
