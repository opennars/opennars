# IO Operators (_draft_)

Data transform and streaming operators

## Streams

Streams are identified by a unique NAL term
representing the immutable data necessary
to resolve it.

### URLs in NAL
Streams which can be referenced by URL can
be expressed by:

```
source(location)
```

a device canonically represented as a succession of
inheritances uniquely describing it. items which
are logically associated should be grouped appropriately
with products or sets.


```
file("tmp","file.txt")
http(("website.org",80):("path0","path1"))
ssh(("localhost",22):("user","pass"))
```


## Stream pipeline

A composition or pipeline of streams
is also a stream:

```
last(middle(first(source)))
```

Other options:

```
pipeline(source, (&/, first, middle, last))

pipeline(source, (first, middle, last))

(&/,first,middle,last)(source)
```

ex: HTTP response interpreted as JSON object
```
json(http(
    ("website.org",80):
        ("path0","path1"))
)
```

ex: .sh file
 * this would be interpeted as the
 desire to execute a shell script with sh
```

 out(
    exec_sh(file("tmp", "script.sh"))),
    (&/, "whoami")
 )
```



see: MIME types


### Internet Specifications
 * http://www.w3.org/Addressing/URL/url-spec.txt
 * https://docs.oracle.com/javase/8/docs/api/java/net/URL.html
 * https://en.wikipedia.org/wiki/OSI_model
 * MIME




## Connection

#### Connect
```
 connect(stream[, callback])!
```
#### Disconnect
```
 --connect(stream)!
```
 stream implementations can interpret
 the desire levels of their connection
 in various ways:

 * explicit connect / disconnect on the rise/fall of the desire state
 * proportional to the bandwidth, with a minimum threshold for connection state


 the strongest desire vectors would
 indicate to the io-manager those of which it
 knows that the system should attempt to connect/
 maintain connection, and those which it should
 not.  likewise any type of namespaced resource
 like a URI, or REST verbs can all be indicated


 variations of connection goals would be permutable
 allowing the system to explore the boundaries of
 possibility.


## I/O desire


 * Each stream implementation can determine
the appropriate conversion to its wire format of
output and input terms terms.


### Input
```
    in(stream, (&/, messages, ...) )!
```

while the input is buffering, it may
batch messages into groups as part of the
sequence:

  * sequence - for hierarchical represntation of time)

  * extset or parallel - for indicating the uncertainty of event arrival. ex: if the events occurr faster than temporal resolution could have distinguished their order. indicating parallelizability
    * can be parallelized

a set of messages can be processed
in parallel. and also de-duplicated
as the buffer grows.

ex:
```
    in(stream, (&/, "subject", (&/, sentence1, ..., sentenceN ))?
    in(stream, (&/, {S,E,E}, {T,H,I,S}))?
```

### Output

```
    out( stream, (&/, message)[, callback]  )!
```
where callback is an optional term to be used as the subject of a feedback belief about goal's success or error state.

policies for repeat transmissions are analogous
to the repeat rate of a sustained keyboard press.
there is a threshold delay after which applied
pressure triggers a repeat mode.

there needs to be some time to allow the desire
to "debounce" beyond which a repeat of a discrete
transmit event will be suppressed, otherwise any
desire is likely to result in an unpredictable
number of repeat executions.



### Ping Example

```
<in(stream, (&/, "PING")) =/> out(stream, (&/, PONG))>.
```



### Control

#### Set parameters
```
stream(stream, { param1:value1, ..., paramN: valueN } )!
```

#### Get parameters

inspection and control of stream properties

```
stream(stream, { param1:#x } )?
```

certain implementation details which are not
directly managed by the reasoner.

* system-wide QoS policy with regard to the
balance of bandwidth on a per-resource
basis.

 * parameter configuration

   * transmission rate min/max
   * receive rate min/max
   * timing (wait, reconnect period)
   * security


 * success/error quality state

 * dialog patterns which can be used to learn


## String transform
Pattern template operators to construct strings in various ways:

```
 str({PATTERNS...}, (&/,SEQUENCE,...), #RESULT)
```
ex:
```
 str({A:"abc", B:"xyz" }, (&/,"i", A, " am experiencing ", B), #STRING )!
```

## String extract

```
str((&/,SEQUENCE,...), {PATTERNS...}, #RESULT)
```

example :
```
str("i abc am experiencing xyz", {"abc":#name, "xyz":#it} )!
```

 * can support regex and multiple results (as a product in sequence order)



## Signal experiencing

 * Available signals which it can decide to replay / re-experience, ex: to refresh its memory


## Autonomic streams

```
<stream =/> PING> =/> <PONG =/> stream>>.
<<udp:$remote =/> PING> =/> <PONG =/> udp:$remote>>.
```

Autonomic stream goals react to changes in the
desire state of concepts involving their identifiers,
without involving any operator.  This might be
less expensive for reasoning as it unifies
the stream events at the term level,
and not the task level as operations involve.


## Protocol learning

 * automatic training curriculum and tests
 * learn from observing existing programs (ex: traffic logs)

## Conversation
Can be inspired by other cognitive agent specifications (see: FIPA Agent Communication Language)

 * send
 * confirm
 * request
 * analyze
 * archive
 * operations

## Implementation

Netty and similar libraries supports all I/O features necessary.  For example:

https://github.com/netty/netty/issues/3218#issuecomment-66256837
 * keep-alive for connections (must be pluggable as it is depending on the Protocol)
 * health-check of the connection (must be pluggable as it is depending on the Protocol)
 * Per remote limit for a pool
 * limit for the pool itself
 * Pluggable choosing strategy of the next not used connection out of the pool
 * Fixed size pool vs. lazy creating connections
 * Pluggable strategy to when a connection can be reused and so put back in the pool vs. when it needs to be closed (for example HTTP Connection: close header).
 * metrics
 * regions (so I can flush them independently)
 * pluggable pool key strategy

## Notes
```
<(&&, commonCondition, {
       specificConditionA,
       specificConditionB
   }) =/> commonConclusion>.
```