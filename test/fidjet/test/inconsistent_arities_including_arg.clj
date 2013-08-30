(ns fidjet.test.inconsistent-arities-including-arg)

(defn different-arities
  "A fn which has a different first arg at different arities, one of which is 'config'"
  ([x] (different-arities {:fn inc} x))
  ([config x] ((:fn config) x)))
