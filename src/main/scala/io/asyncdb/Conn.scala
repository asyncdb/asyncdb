package io.asyncdb


import cats.effect.IO

/**
* A database connection, send an statement or query, returns an IO
*/
trait Conn[R] {
  def sendQuery(sql: String): IO[R]
  def sendPreparedStatement(stmt: String, params: List[Any]): IO[R]
}

trait StreamConn[R] extends Conn[R] {

}
