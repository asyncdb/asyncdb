package io.asyncdb

trait Error                     extends Throwable
trait ClientError               extends Error
trait ServerError               extends Error
trait IOError                   extends Error
case class Timeout(msg: String) extends IOError
case object EOF                 extends IOError
