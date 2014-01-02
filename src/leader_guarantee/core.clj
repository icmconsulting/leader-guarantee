(ns leader-guarantee.core
	(:import (org.jgroups.blocks.locking LockService)
					 (org.jgroups.protocols CENTRAL_LOCK)
					 (org.jgroups JChannel)
					 (java.util.concurrent.locks Lock)))

(def get-lock-threads (atom {}))

(defn- get-lock
	[lock-service lock-promise]
	(fn []
		(let [lock (.getLock lock-service "leader")]
			(do
				(.lock lock)
				(deliver lock-promise :is-leader)))))

(defn- leader-thread
	[cluster-name]
	(locking get-lock-threads
		(if-not (get @get-lock-threads cluster-name)
			(let [channel (doto (JChannel.) ;; Uses udp by default
											(.. getProtocolStack (addProtocol (doto (CENTRAL_LOCK.) (.init))))
											(.connect cluster-name))
						lock-service (LockService. channel)
						lock-promise (promise)]
				(get (swap! get-lock-threads
							 assoc
							 cluster-name
							 {:thread
								(doto (Thread. (get-lock lock-service lock-promise) "LeaderAppointmentThread")
									(.setDaemon true)
									(.start))
								:promise lock-promise})
						 cluster-name))
			(get @get-lock-threads cluster-name))))

(defn when-leader*
	"Executes f when the local application container becomes leader of the cluster-name cluster."
	[cluster-name f]
	(let [leader-promise (:promise (leader-thread cluster-name))]
		(if (realized? leader-promise)
			(f)
			(future
				(when (deref leader-promise) (f))))))

(defmacro when-leader
  "Execute body forms WHEN the local application container has been appointed as the leader
	of the cluster named in cluster-name.
	Can be executed multiple times within a container, and when the node becomes leader, will
	execute each body.
	The leader will stay leader for the entire lifetime of the JVM - only releasing control
	when the JVM is shut down.
	Returns the result of executing body, OR a future that will execute body when the node becomes leader of the cluster.

		Note: if the leader isn't the cluster leader when first called, the body will be executed in a future."
  [cluster-name & body]
	`(letfn [(when-leader# [] (do ~@body))]
		(when-leader* ~cluster-name when-leader#)))
