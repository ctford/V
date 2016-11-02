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

(defn check
  "Apply a predicate to a validation value, returning the original value if it succeeds or an error if it fails."
  ([x] x)
  ([x ok? error & other-checks]
   (cond
     (errors x) x
     (-> x value ok?) (apply check x other-checks)
     :otherwise (->> other-checks (apply check x) errors (apply failure error)))))

(defn check-not
  "Apply a predicate to a validation value, returning the original value if it fails or an error if it fails."
  [x ok? error]
  (check x (complement ok?) error))

(defn extract
  "Apply a function to a validation value, returning an error on nil."
  [x f error]
  (-> (fmap f x) (check-not nil? error)))

(defn default
  "Give a validation value a default value if it's an error."
  [x v]
  (if (errors x) (success v) x))

(defmacro catch-exception
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type f error & args]
  `(try
     (fmap ~f ~@args)
     (catch ~exception-type ~'_
       (failure ~error))))
