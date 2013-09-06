(ns fidjet.test.lazy-seq-configured
  (:require [fidjet.test.lazy-seq]
            [fidjet.core :as f]))

(f/remap-ns-with-arg fidjet.test.lazy-seq config)
