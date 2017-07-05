package io.asyncdb

import cats.effect.IO
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.ExecutorService
import fs2._
import fs2.io.tcp
import fs2.async.mutable.Queue
import scala.concurrent.ExecutionContext
import scala.util._


/**
* A database connection, send an command, returns an IO
*/

trait Conn[I, O] {
  def send(cmd: I): IO[O]
}

object Conn {

  case class Config(
    server: InetSocketAddress,
    group: AsynchronousChannelGroup,
    threadPool: ExecutorService,
    queueSize: Int
  )

  def connect[I, O](config: Config)(setup: tcp.Socket[IO] => IO[Unit], encode: I => tcp.Socket[IO] => IO[O]) = {

    implicit val AG = AsynchronousChannelGroup.withThreadPool(config.threadPool)
    implicit val EC = ExecutionContext.fromExecutorService(config.threadPool)

    Queue.unbounded[IO, tcp.Socket[IO] => IO[O]].map { queue =>

      val clientLoop = tcp.client[IO](config.server).flatMap { socket =>
        Stream.eval(setup(socket)) ++ queue.dequeue.evalMap { f =>
          f(socket)
        }
      }

      new Conn[I, O] {
        def send(cmd: I): IO[O] = IO.async { (cb) =>
          def encodeIO(cmd: I)(socket: tcp.Socket[IO]) = {
            encode(cmd)(socket).map(o => cb(Right(o)))
          }
        }
        clientLoop.run.unsafeRunAsync { r =>
        }
      }
    }.unsafeRunSync
  }
}
