(ns fidjet.test.simple-fns-with-config)

(defn foo
  "A function which does not accept a configuration. Used for testing."
  [x]
  x)

(defn bar
  "A function which does accept a configuration. Used for testing."
  [config x]
  ((:fn config) x))
