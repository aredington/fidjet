(ns fidjet.test.simple-fns-with-config-configured
  (:require [fidjet.test.simple-fns-with-config]
            [fidjet.core :as f]))

(f/remap-ns-with-arg fidjet.test.simple-fns-with-config config)

