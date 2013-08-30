(ns fidjet.test.destructured-first-args-with-name)

(defn foo
  [{f :fn :as config} x] (f x))

(defn destructuring-first
  [[first & rest :as seq]]
  first)
