package io.asyncdb
package nio

import java.nio.channels.CompletionHandler

object Handler {

  def apply[A](onCompleted: => Unit)(
    onFailed: Throwable => Unit): CompletionHandler[A, Any] = {
    apply[A]((t: A) => { onCompleted })(onFailed)
  }

  def apply[A, V](onCompleted: A => Unit)(
    onFailed: Throwable => Unit): CompletionHandler[A, Any] =
    new CompletionHandler[A, Any] {
      def completed(n: A, x: Any) = {
        onCompleted(n)
      }

      def failed(t: Throwable, x: Any) = {
        onFailed(t)
      }
    }
}
