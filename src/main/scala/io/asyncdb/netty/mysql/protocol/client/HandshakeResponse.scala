package io.asyncdb
package netty
package mysql
package protocol
package client


/**
 * HandshakeResponse41
 */
case class HandshakeResponse(
  clientFlag: Int,
  maxPacketSize: Int,
  charset: Short,
  filter: Array[Byte],
  username: String,

)

sealed trait AuthResponse
case class LenencClientData(authResponse: String)
case class LegacyClientData(authResponse: String)

object HandshakeResponse {
  import Encoder._

  implicit val handshakeResponseEncoder: Encoder[HandshakeResponse] =
    new Encoder[HandshakeResponse] {

      def encode(v: HandshakeResponse) = {
        intL4 :: intL4 :: uint1 :: uint1 :: {
          if(v.clientFlag & LongPassword)
        }
        ???
      }
    }

}
