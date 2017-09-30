package io.asyncdb

import cats.effect.IO
import scala.concurrent.duration._

/**
 * Timeout in seconds
 */
trait Socket {
  def readBytes(n: Int, timeout: Long): IO[Array[Byte]]
}
