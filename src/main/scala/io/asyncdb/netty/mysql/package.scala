package io.asyncdb
package netty

import cats.effect.concurrent._

package object mysql {
  type MsgRef[F[_]] = MVar[F, Either[Throwable, Message]]
}
