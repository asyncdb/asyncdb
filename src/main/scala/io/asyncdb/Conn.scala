package io.asyncdb

import cats.data.NonEmptyList
import cats.free.Free

sealed trait ConnA[T]
case class SendQuery(sql: String) extends ConnA[Either[Throwable, Any]]
case class SendPreparedStatement(sql: String, param: NonEmptyList[Any])
    extends ConnA[Either[Throwable, Any]]
case object Close extends ConnA[Either[Throwable, Any]]
