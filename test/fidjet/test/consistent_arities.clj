(ns fidjet.test.consistent-arities)

(defn foo
  ([config x] ((:fn config) x))
  ([config x y] ((:fn config) (* x y))))

