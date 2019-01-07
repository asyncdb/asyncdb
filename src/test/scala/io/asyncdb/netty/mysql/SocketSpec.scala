package io.asyncdb
package netty
package mysql

import cats.effect._
import io.netty.bootstrap._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio._
import java.net.InetSocketAddress
import org.scalatest._
import scala.concurrent.{Future, ExecutionContext}
import scala.language.implicitConversions


abstract class SocketSpec extends AsyncFreeSpec with Matchers {

  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  implicit def effectAsFuture[A](f: IO[A]) = f.unsafeToFuture
  val host: String = "127.0.0.1"
  val port: Int = 3306

  def withSocket[A](f: MySQLSocket[IO] => IO[A]): IO[A] = {
    val address = new InetSocketAddress(host, port)
    val b = (new Bootstrap)
      .remoteAddress(address)
      .group(new NioEventLoopGroup())
      .channel(classOf[NioSocketChannel])
    Resource.make(MySQLSocket[IO](b).flatMap(_.connect))(_.disconnect).use(f)
  }

}
