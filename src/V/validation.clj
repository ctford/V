(ns V.validation
  (:require [clojure.set :as set]))

(defn success
  "Return a successful validation value."
  [x]
  [:value x])

(defn value
  "Return the value of a validation value, or nil if it's a failure."
  [[k v]]
  (when (= k :value) v))

(defn failure
  "Return a failure validation value."
  [& errors]
  [:errors (set errors)])

(defn errors
  "Return the errors of a validation value, or nil if it's a success."
  [[k v]]
  (when (= k :errors) v))

(defn v-apply
  "Apply a function to validation values."
  [f args]
  (if-let [combined-errors (->> args (map errors) (reduce set/union nil))]
    (apply failure combined-errors)
    (->> args (map value) (apply f))))

(defn fmap
  "Apply a function to validation values, returning a validation value on the assumption of success."
  [f & args]
  (v-apply (comp success f) args))

(defn fmap*
  "Lift a function to apply to validation values, returning a validation value on the assumption of success."
  [f]
  (partial fmap f))

(defn lift-let [syms]
  (mapcat
    (fn [[f xs]] (interleave xs (map (partial list f) xs)))
    (partition 2 syms)))

(defmacro lift
  "Shadow fs with lifted versions of themselves within a lexical scope."
  [bindings & body]
  `(let ~(vec (lift-let bindings))
     ~@body))

(defn check*
  "Apply a predicate to a validation value, returning the original value if it succeeds or an error if it fails."
  [ok?]
  (fn [error]
    (fn [x]
      (cond
        (errors x) x
        (-> x value ok?) x
        :otherwise (failure error)))))

(defn check
  "Apply a predicate to a validation value, returning the original value if it succeeds or an error if it fails."
  [x ok? error]
  (((check* ok?) error) x))

(defn unless
  "Return v unless there are errors in vs."
  [v & checks]
  (v-apply (constantly v) ((apply juxt checks) v)))

(defn extract
  "Apply a function to a validation value, returning an error on nil."
  [x f error]
  (-> (fmap f x) (check (comp not nil?) error)))

(defn default
  "Give a validation value a default value if it's an error."
  [x v]
  (if (errors x) (success v) x))

(defmacro catch-exception*
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type]
  `(fn [f#]
     (fn [error# & args#]
       (try
         (apply fmap f# args#)
         (catch ~exception-type _#
           (failure error#))))))

(defmacro catch-exception
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type f error & args]
  `(((catch-exception* ~exception-type) ~f) ~error ~@args))
