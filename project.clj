(defproject double-booked "0.1.0-SNAPSHOT"
  :description "Doubled booked solution by Michał Psota"
  :url ""
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :repl-options {:init-ns double-booked.core}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]]}})
