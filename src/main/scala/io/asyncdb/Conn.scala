package io.asyncdb

import cats.data.NonEmptyList
import cats.free.Free

sealed trait ConnA[T]
case class SendQuery(sql: String) extends ConnA[Either[Throwable, Result]]
case class SendPreparedStatement(sql: String, param: NonEmptyList[Data])
    extends ConnA[Either[Throwable, Result]]
case object Close extends ConnA[Either[Throwable, Result]]
