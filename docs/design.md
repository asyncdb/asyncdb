# Design overview

+ Core Types

+ `type Reads[A] = Socket => IO[A]`
+ `type Writes[A] = (Socket, A) => IO[Unit] `
