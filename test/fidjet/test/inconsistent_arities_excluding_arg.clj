(ns fidjet.test.inconsistent-arities-excluding-arg)

(defn foo
  ([x y] (* x y))
  ([z y x] (- (* y x) z)))

(defn blat
  [config x]
  ((:fn config) x))
