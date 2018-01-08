package io.asyncdb.util

import java.util.concurrent.atomic.AtomicReference
import cats.effect._

final case class Change[A](prev: A, now: A)

final class Ref[F[_], A](private val ar: AtomicReference[A])(
  implicit F: Sync[F]) {

  def get: F[A] = F.delay {
    ar.get()
  }

  def tryModify(f: A => A): F[Option[Change[A]]] = F.delay {
    val old     = ar.get()
    val now     = f(old)
    val success = ar.compareAndSet(old, now)
    if (success) Some(Change(old, now)) else None
  }
}
