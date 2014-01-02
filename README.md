# Leader Guarantee

Guarantee a single "leader" node across a cluster of nodes.

Run a function when node becomes leader.

See https://en.wikipedia.org/wiki/Leader_election

## Install
In leiningen:

```
[leader-guarantee "0.2.0"]
```

In Maven:

```
<dependency>
  <groupId>leader-guarantee</groupId>
  <artifactId>leader-guarantee</artifactId>
  <version>0.2.0</version>
</dependency>
```


## How to use
It's pretty easy.

```clojure
 (use 'leader-guarantee.core)

 (when-leader "my-cluster"
 	(println "I am leader! Feel my wrath!")
 	(initialise-something-that-can-only-run-on-one-node))

;; => If the node is currently the leader, when-leader will return the result of executing the body.
;; => If the node is NOT currently the leader, when-leader will return a future that will be pending until/if the node becomes leader of the cluster.
```

The leader will be the leader of the cluster for it's JVM's entire life. When the host JVM shuts down, then another node
in the cluster will take over.

when-leader can be executed multiple times within an application for the same cluster, and all bodys will be run when the node becomes leader.

An application node can attempt to become leader in multiple clusters, differentiated using the cluster name.

## What?
Leader-gurantee (LG) is just a simple wrapper around [JGroups'](http://jgroups.org) [LockService](http://www.jgroups.org/javadoc/org/jgroups/blocks/locking/LockService.html).

+ LG uses UDP as its transport protocol - it uses the default UDP config, plus:
+ LG uses a Central controlled lock - see [CENTRAL_LOCK](http://www.jgroups.org/javadoc/org/jgroups/protocols/CENTRAL_LOCK.html)

## Help!

### I get a "SEVERE: JGRP000029" with a IPv6 destination address error consistently in my logs - it still seems to work, though
Annoying! Easy to fix, though - just force the JVM to use IP4:

```
-Djava.net.preferIPv4Stack=true
```
Thats not going to suit everyone, though. If someone else has a better way to fix this, let me know...


## License

Copyright © 2014 ICM Consulting Pty Ltd. http://www.icm-consulting.com.au

Distributed under the Eclipse Public License, the same as Clojure.
