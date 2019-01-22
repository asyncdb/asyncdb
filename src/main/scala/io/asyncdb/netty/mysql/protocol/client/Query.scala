package io.asyncdb
package netty
package mysql
package protocol
package client

import shapeless._

case class Query(
  query: String
) extends Message

object Query {

  import Encoder._

  implicit val queryEncoder: Encoder[Query] =
    Encoder[Query] { data =>
      ntText.productL(_ => Command.Client.Query.toByte, int1).as[Query]
    }
}
