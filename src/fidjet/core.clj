(ns ^{:doc "Fidjet: Making pure fns with configurations less painful."
      :author "Alex Redington"}
  fidjet.core
  (:require [clojure.set :as s]))

(defn fn-sym-vars
  "Return the names and vars of all fns defined in ns"
  [ns]
  (filter (comp fn? deref second) (ns-publics ns)))

(defn fn-sym-vars-with-arg
  "Return the names and vars of all fns that accept arg-sym as their
  first arg in ns"
  [ns arg-sym]
  (filter
   ;; Thise absurdly convoluted predicate will dig through ns, and
   ;; find every fn where every arity receives arg-sym as its first
   ;; arg.
   (comp (partial every? #(= arg-sym (first %))) :arglists meta second)
   (fn-sym-vars ns)))

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
