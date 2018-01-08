package io.asyncdb

sealed trait Error              extends Throwable
sealed trait ClientError        extends Error
sealed trait ServerError        extends Error
sealed trait IOError            extends Error
case class Timeout(msg: String) extends IOError
case object EOF                 extends IOError
