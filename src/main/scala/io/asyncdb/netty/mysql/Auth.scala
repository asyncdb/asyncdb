package io.asyncdb
package netty
package mysql

import java.security.MessageDigest
import java.nio.charset.Charset
import io.netty.buffer._

object Auth {

  def nativePassword(
    seed: Array[Byte],
    password: String,
    charset: Charset
  ): Array[Byte] = {
    val md = MessageDigest.getInstance("SHA-1")
    val hash1 = md.digest(password.getBytes(charset))
    md.reset()
    val hash2 = md.digest(hash1)
    md.reset()
    md.update(seed)
    md.update(hash2)
    val digest = md.digest()
    (0 until digest.length) foreach { i =>
      digest(i) = (digest(i) ^ hash1(i)).toByte
    }
    digest
  }
}
