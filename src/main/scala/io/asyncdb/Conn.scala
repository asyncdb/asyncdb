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
    threadPool: ExecutorService
  )

  def connect[I, O](config: Config)(encode: I => tcp.Socket[IO] => IO[O], queue: Queue[IO, tcp.Socket[IO] => IO[Unit]]) = {

    implicit val AG = AsynchronousChannelGroup.withThreadPool(config.threadPool)
    implicit val EC = ExecutionContext.fromExecutorService(config.threadPool)

    val clientLoop = tcp.client[IO](config.server).flatMap { socket =>
      def loop(): Stream[IO, Unit] = Stream.eval(queue.dequeue1.flatMap(_(socket))) ++ loop()
      loop()
    }

    clientLoop.run.map { l =>
      new Conn[I, O] {
        def send(cmd: I): IO[O] = IO.async { cb =>
          def ei(i: I)(s: tcp.Socket[IO]) = {
            encode(i)(s).attempt.map(cb)
          }
          queue.enqueue1(ei(cmd))
        }
      }
    }
  }
}
