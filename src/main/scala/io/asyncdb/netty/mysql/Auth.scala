package io.asyncdb
package netty
package mysql

import java.security.MessageDigest
import java.nio.charset.Charset

object Auth {

  def nativePassword(
    seed: Array[Byte],
    password: String,
    charset: Charset
  ): Array[Byte] = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    val initialDigest = messageDigest.digest(password.getBytes(charset))
    messageDigest.reset()
    val finalDigest = messageDigest.digest(initialDigest)
    messageDigest.reset()
    messageDigest.update(seed)
    messageDigest.update(finalDigest)
    val result = messageDigest.digest()
    (result.zip(initialDigest)).map {
      case (a, b) => (a & b).toByte
    }
  }
}
