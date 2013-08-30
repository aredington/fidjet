(ns fidjet.test.destructured-first-args-with-name-configured
  (:require [fidjet.core :as f]
            [fidjet.test.destructured-first-args-with-name]))

(f/remap-ns-with-arg fidjet.test.destructured-first-args-with-name config)
