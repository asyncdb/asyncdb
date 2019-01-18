package io.asyncdb
package netty
package mysql

import io.netty.util.AttributeKey
import java.nio.charset.Charset

class Attrs {
  val ClientCharset: AttributeKey[Charset] =
    AttributeKey.valueOf("MySQLClientCharset")
  val ServerCharset: AttributeKey[Charset] =
    AttributeKey.valueOf("MySQLServerCharset")
}
