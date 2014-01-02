(ns leader-guarantee.core
	(:import (org.jgroups.blocks.locking LockService)
					 (org.jgroups.protocols CENTRAL_LOCK)
					 (org.jgroups JChannel)
					 (java.util.concurrent.locks Lock)))

(defn- get-lock
	[lock-service with-lock-fn]
	(fn []
		(let [lock (.getLock lock-service "leader")]
			(do
				(.lock lock)
				(with-lock-fn)))))

(defn when-leader*
	"Executes with-lock-fn when the local application container becomes leader of the cluster-name cluster."
	[cluster-name with-lock-fn]
	(let [channel (doto (JChannel.) ;; Uses udp by default
									(.. getProtocolStack (addProtocol (doto (CENTRAL_LOCK.) (.init))))
									(.connect cluster-name))
				lock-service (LockService. channel)]
		(doto (Thread. (get-lock lock-service with-lock-fn) "LeaderAppointmentThread")
			(.setDaemon true)
			(.start))))

(defmacro when-leader
  "Execute body forms IFF the local application container has been appointed as the leader
	of the cluster named in cluster-name.
	The leader will stay leader for the entire lifetime of the JVM - only releasing control
	when the JVM is shut down."
  [cluster-name & body]
	`(letfn [(on-leader# [] (do ~@body))]
		(when-leader* ~cluster-name on-leader#)))
