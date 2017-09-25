# Design overview

+ Core Types

+ `type Decoder[A] = Buf => Either[E, A]`
+ `type Encoder[A] = (Buf, A) => Unit `
