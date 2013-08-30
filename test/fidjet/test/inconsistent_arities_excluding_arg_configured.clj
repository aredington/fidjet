(ns fidjet.test.inconsistent-arities-excluding-arg-configured
  (:require [fidjet.test.inconsistent-arities-excluding-arg]
            [fidjet.core :as f]))

(f/remap-ns-with-arg fidjet.test.inconsistent-arities-excluding-arg config)
