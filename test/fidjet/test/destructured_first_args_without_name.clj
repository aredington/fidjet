(ns fidjet.test.destructured-first-args-without-name)

(defn foo
  [{f :fn} x] (f x))

(defn destructuring-first
  [[first & rest]]
  first)
