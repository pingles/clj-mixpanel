(defproject clj-mixpanel "0.0.3"
  :description "Send events to Mixpanel"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.4.2"]
                 [org.clojure/data.json "0.1.2"]
                 [commons-codec/commons-codec "1.6"]
                 [org.clojure/tools.logging "0.2.3"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]
                                  [org.slf4j/slf4j-simple "1.6.4"]]}}
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]]
  :min-lein-version "2.0.0")
