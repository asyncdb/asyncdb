# Design overview

State purely as far as possible.

## MySQL Socket Implementation

A ```StateMachine``` is introduced to process messages according to current state,
it is basically a partial function: ```ByteBuf => StateT[ChannelContext, ChannelState.Result]```

When a new message arrive, it was passwd the ```StateMachine```.

It returns two message:

+ Outgoing message(will be send to server if any)
+ Output message(will be received by ```MySQLSocket.read``` call if any)
