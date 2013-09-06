(ns fidjet.test.lazy-seq)

(defn foos
  [config]
  (lazy-seq (cons (:ele config)
                  (foos config))))
