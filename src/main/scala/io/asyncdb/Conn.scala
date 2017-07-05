package io.asyncdb

import cats.effect.IO
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import fs2._


/**
* A database connection, send an statement or query, returns an IO
*/
trait Conn[R] {
  def sendQuery(sql: String): IO[R]
  def sendPreparedStatement(stmt: String, params: List[Any]): IO[R]
}

object Conn {

  case class Config(
    address: String,
    group: AsynchronousChannelGroup
  )

  def connect[A](config: Config)(setup: IO[Unit], loop: Stream[IO, A]) = {

  }
}
