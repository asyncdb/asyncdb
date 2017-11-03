package io.asyncdb

sealed trait Error
sealed trait ClientError extends Error
sealed trait ServerError extends Error
