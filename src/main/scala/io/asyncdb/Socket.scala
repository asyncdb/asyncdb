package io.asyncdb

import cats.effect.IO
import scala.concurrent.duration._

/**
 * Timeout in seconds
 */
trait Socket[I, O] {
  def read: IO[O]
}
