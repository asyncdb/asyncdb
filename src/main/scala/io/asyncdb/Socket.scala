package io.asyncdb

import cats.effect.IO
import java.nio.ByteBuffer
import java.nio.channels._
import java.net.SocketAddress
import java.util.concurrent.ExecutorService


trait Socket {
  def connect(): IO[Unit]
  def close(): IO[Unit]
}

trait NioSocket extends Socket {
val E: ExecutorService
  val channel: AsynchronousSocketChannel



  type CH = AsynchronousSocketChannel

  def connect(remote: SocketAddress): IO[CH] = {
    IO.async { cb =>
      channel.connect(remote, channel, new CompletionHandler[Void, CH] {
        def completed(result: Void, ch: CH ) = {
          cb(Right(ch))
        }
        def failed(exc: Throwable, ch: CH) {
          cb(Left(exc))
        }
      })
    }
  }

  def close: IO[Unit] = {
    IO.async { cb =>
      E.submit(new Runnable {
        def run() = {
          try {
            channel.shutdownInput()
            channel.close()
            cb(Right({}))
          } catch {
            case e: Throwable =>
              cb(Left(e))
          }
        }
      })
    }
  }
}
