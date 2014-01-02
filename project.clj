(defproject leader-guarantee "0.1.0-SNAPSHOT"
  :description "Determine and guarantee a single leader node across a cluster"
  :url "https://github.com/bbbates/leader-guarantee"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
								 [org.jgroups/jgroups "3.4.1.Final"]
 								 [org.clojure/tools.logging "0.2.6"]]
	:jvm-opts ["-Djava.net.preferIPv4Stack=true"]
	)
