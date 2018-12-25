package io.asyncdb

import cats.effect._
import io.netty.channel.{ChannelFuture, ChannelFutureListener}

package object netty {
  implicit class ChannelFutureOps(val future: ChannelFuture) extends AnyVal {
    def to[F[_]](implicit F: Concurrent[F]): F[ChannelFuture] = F.cancelable { k =>
      val f = future.addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture): Unit = {
          if(future.isSuccess) k(Right(future)) else k(Left(future.cause()))
        }
      })
      F.delay(f.cancel(false))
    }
  }
}
