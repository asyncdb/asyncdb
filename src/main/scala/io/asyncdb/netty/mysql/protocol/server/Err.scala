package io.asyncdb
package netty
package mysql
package protocol
package server

case class Err(
  header: Byte,
  errcode: Int,
  sqlStateMarker: Byte,
  sqlState: String,
  errorMessage: String
) extends Exception
    with ServerMessage {
  override def getMessage =
    s"Err(header: ${header}, code:${errcode}, marker: ${sqlStateMarker}, state:${sqlState},errorMessage: ${errorMessage})"
}

object Err {

  import Decoder._

  implicit val errDecoder: Decoder[Err] = {
    (int1 :: intL2 :: int1 :: str(5) :: strEOF).as[Err]
  }
}
