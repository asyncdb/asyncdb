package io.asyncdb
package netty
package mysql

sealed trait InitState

object InitState {
  case object WaitHandshakeInit extends InitState
  case object ReceivedHandshakeInit extends InitState
  case object ReceivedLoginResponse extends InitState
}
