(defproject fidjet "0.0.3-SNAPSHOT"
  :description "One stop happiness for making your configured functions pure."
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :license {:name "MIT"
            :url "https://raw.github.com/aredington/fidjet/master/LICENSE"
            :distribution :repo}
  :url "http://github.com/aredington/fidjet"
  :profiles {:dev  {:dependencies [[midje "1.5.1"]]
                    :plugins [[lein-midje "3.1.0"]]}})
