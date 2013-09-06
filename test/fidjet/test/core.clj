(ns fidjet.test.core
  (:require [fidjet.core :as f]
            [fidjet.test.inconsistent-arities-including-arg]
            [fidjet.test.destructured-first-args-without-name]
            [fidjet.test.simple-fns-with-config-configured :as c1]
            [fidjet.test.inconsistent-arities-excluding-arg-configured :as c2]
            [fidjet.test.consistent-arities-configured :as c3]
            [fidjet.test.destructured-first-args-with-name-configured :as c4]
            [fidjet.test.lazy-seq-configured :as c5])
  (:use [midje.sweet]))

(facts "about `fidjet.test.simple-fns-with-config-configured`"
       (fact "`foo` can be called outside a config block"
             (c1/foo 1) => 1)
       (fact "`bar` cannot be called outside a config block"
             (c1/bar 1) => (throws IllegalStateException
                                   #"must be called from within a with-"))
       (fact "`foo` can be called inside a config block"
             (c1/with-config {:fn inc}
               (c1/foo 1)) => 1)
       (fact "`bar` can be called inside a config block"
             (c1/with-config {:fn inc}
               (c1/bar 1)) => 2))

(facts "about `fidjet.test.inconsistent-arities-including-arg`"
       (fact "trying to macroexpand remap-ns-with-arg throws"
             (eval '(f/remap-ns-with-arg fidjet.test.inconsistent-arities-including-arg config))
             =>
             (throws clojure.lang.ExceptionInfo #"Inconsistent arities")))

(facts "about `fidjet.test.inconsistent-arities-excluding-arg-configured`"
       (facts "the fn with inconsistent arities"
              (fact "works in the 2-arity"
                    (c2/foo 6 2) => 12)
              (fact "works in the 3-arity"
                    (c2/foo 2 6 3) => 16))
       (facts "the fn with the arg-sym"
              (fact "works in a with-config block"
                    (c2/with-config {:fn inc}
                      (c2/blat 3)) => 4)))

(facts "about `fidjet.test.consistent-arities-configured`"
       (facts "the `foo` fn"
              (fact "works in 2-arity with-config"
                    (c3/with-config {:fn inc}
                      (c3/foo 7)) => 8)
              (fact "works in the 3-arity with-config"
                    (c3/with-config {:fn dec}
                      (c3/foo 6 3)) => 17)))

(facts "about `fidjet.test.destructured-first-args-without-name`"
       (fact "trying to macroexpand remap-ns-with-arg throws"
             (eval '(f/remap-ns-with-arg fidjet.test.destructured-first-args-without-name config))
             =>
             (throws clojure.lang.ExceptionInfo #"Anonymous destructured first argument")))

(facts "about `fidjet.test.destructured-first-args-with-name-configured`"
       (facts "the `foo` fn"
              (fact "throws outside a with-config block"
                    (c4/foo 13)
                    =>
                    (throws IllegalStateException
                            #"must be called from within a with-"))
              (fact "operates correctly in a with-config block"
                    (c4/with-config {:fn inc}
                      (c4/foo 13)) => 14)))

(facts "about `fidjet.test.lazy-seq-configured`"
       (fact "with-config returns lazy-seqs that retain the scope of the with-config block"
             (let [seq (c5/with-config {:ele :zot}
                         (c5/foos))
                   drop-realized (fn [seq]
                                   (if (realized? seq)
                                     (recur (rest seq))
                                     seq))]
               (first (drop-realized seq))) => :zot))

