package io.asyncdb
package netty
package mysql

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import cats.data.NonEmptyList

class MySQLSocket[F[_]](config: NettySocketConfig[F], state: Ref[F, Option[Deferred[F, NonEmptyList[Packet]]]])
  (implicit F: Concurrent[F]) extends NettySocket[F, NonEmptyList[Packet]](config) {

  def read() = state.get.flatMap {
    case None => F.raiseError(new Exception("Nothing to read now"))
    case Some(d) => d.get
  }

  def write(n: NonEmptyList[Packet]) = {
    n.traverse(writePacket).void
  }

  private def writePacket(p: Packet) = {
    channel.flatMap { ch =>
      ch.write(p).to[F]
    }
  }
}
