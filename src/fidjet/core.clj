(ns ^{:doc "Fidjet: Making pure fns with configurations less painful."
      :author "Alex Redington"}
  fidjet.core
  (:require [clojure.set :as s])
  (:import (clojure.lang Symbol IPersistentMap IPersistentVector)))

(defn fn-sym-vars
  "Return the names and vars of all fns defined in ns"
  [ns]
  (filter (comp fn? deref second) (ns-publics ns)))

(defprotocol FnArg
  (arg-name [arg] "Returns the symbol argument name of arg. If an
  argument name cannot be determined, throws."))

(extend-protocol FnArg
  Symbol
  (arg-name [symbol] symbol)
  IPersistentMap
  (arg-name [m]
   (if (find m :as)
     (:as m)
     (throw (ex-info "Anonymous destructured first argument"
                     {:argument m
                      :as-m (:as m)
                      :destructured-as :map}))))
  IPersistentVector
  (arg-name [v]
    (let [length (count v)]
      (if (= :as (v (- length 2)))
        (last v)
        (throw (ex-info "Anonymous destructured first argument"
                        {:argument v
                         :destructured-as :seq}))))))

(defn first-arg-syms
  "Returns the symbols naming the first argument of each arity of the
  fn stored in `fn-var`"
  [fn-var]
  (->> fn-var
       meta
       :arglists
       (map first)
       (map arg-name)))

(defn fn-vars-consistent?
  "Predicate to test `var` for argument consistency against
  `sym`. Argument consistency is true for `var` if

   - No arities include `sym` as the first arg.
   - Every arities includes `sym` as the first arg."
  [var sym]
  (let [first-args (first-arg-syms var)]
    (if (or (every? (partial = sym) first-args)
            (every? (partial not= sym) first-args))
      true
      false)))

(defn first-arg-named?
  "Predicate to test if every arity of a namespace mapping has
  `arg-sym` as a first argument."
  [arg-sym [fn-sym fn-var :as ns-mapping]]
  (let [arg-syms (first-arg-syms fn-var)]
    (every? (partial = arg-sym) arg-syms)))

(defn fn-sym-vars-with-arg
  "Return the names and vars of all fns that accept arg-sym as their
  first arg in ns"
  [ns arg-sym]
  (let [sym-vars (fn-sym-vars ns)
        inconsistent-sym-vars (remove (comp #(fn-vars-consistent? % arg-sym)
                                            second)
                                      sym-vars)]
    (when-not (empty? inconsistent-sym-vars)
      (throw (ex-info (str "Inconsistent arities in " ns)
                             {:inconsistent-arities
                              (into {} (for [[sym var] inconsistent-sym-vars]
                                         [sym (-> var
                                                  meta
                                                  :arglists)]))})))
    (filter
     (partial first-arg-named? arg-sym)
     sym-vars)))

(defn fn-sym-vars-without-arg
  "Return the names and vars of all the fns that don't accept
  arg-sym as their first arg in ns"
  [ns arg-sym]
  (s/difference (set (fn-sym-vars ns)) (set (fn-sym-vars-with-arg ns arg-sym))))

(defmacro make-with-arg-macro
    "Create a with-arg macro for executing all of the matching functions
  from ns with the first argument passed implicitly e.g.:

  (make-with-arg-macro monotony.core config)

  will make a with-config macro against the monotony.core namespace."
  [ns arg-sym]
  (let [symbol-fidjet-gensym (list 'symbol 'fidjet-gensym)]
    `(defmacro  ~(symbol (str "with-" arg-sym))
      ~(str "Evaluate body with all "
            *ns* " functions receiving "
            arg-sym " as their first argument")
      ~(vector arg-sym '& 'body)
      (let [~(symbol 'fidjet-gensym) (gensym "evaled-config")]
        `(let [~~symbol-fidjet-gensym ~~arg-sym]
           (with-bindings
             ~(into {} (for [~(symbol 'var-name) (keys (f/fn-sym-vars-with-arg
                                                         (quote ~ns)
                                                         (quote ~arg-sym)))]
                         [(list 'var (symbol ~(str *ns*) (str ~(symbol 'var-name))))
                          (list 'partial
                                (symbol ~(str ns) (str ~(symbol 'var-name)))
                                ~symbol-fidjet-gensym)]))
             ~@~(symbol 'body)))))))

(defmacro import-api-fns
  "Expose the fns from ns in the namespace where this is called.
  All fns that need a first arg named arg-sym will be masked with
  obnoxious exception throwing behavior."
  [ns arg-sym]
  (letfn [(fail-without-arg [fn-name]
            (fn [& args]
              (throw (IllegalStateException.
                      (str (str fn-name) " must be called from within a with-"
                           (str arg-sym) " block.")))))]
    ;; Import the fns from api-ns
    `(do ~@(for [needs-arg (keys (fn-sym-vars-with-arg (find-ns ns) arg-sym))]
             `(def ~(with-meta needs-arg {:dynamic true}) ~(fail-without-arg needs-arg)))
         ~@(for [imports-fine (fn-sym-vars-without-arg (find-ns ns) arg-sym)]
             `(def ~(first imports-fine) ~(deref (second imports-fine)))))))

(defmacro remap-ns-with-arg
  [ns arg-sym]
  `(do
     (import-api-fns ~ns ~arg-sym)
     (make-with-arg-macro ~ns ~arg-sym)))
