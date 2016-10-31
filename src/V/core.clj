(ns V.core
  (:require [clojure.set :as set]))

(defn success [x] {:value x})
(def value :value)

(defn failure [& errors] {:errors (set errors)})
(def errors :errors)

(defn fmap
  "Apply a function to validation values, returning a validation value on the assumption of success."
  [f & args]
  (if-let [errors (->> args (map errors) (reduce set/union nil))]
    (apply failure errors)
    (->> args (map value) (apply f) success)))

(defn check
  "Apply a predicate to a validation value, returning the original value if it succeeds or an error if it fails."
  ([x] x)
  ([ok? error & other-checks]
   (let [x (last other-checks)]
     (cond
       (errors x) x
       (-> x value ok?) (apply check other-checks)
       :otherwise (->> other-checks (apply check) errors (apply failure error))))))

(defmacro catch-exceptions
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type f error & args]
  `(try
     (fmap ~f ~@args)
     (catch ~exception-type ~'_
       (failure ~error))))

(defmacro catch-all-exceptions
  "Apply a function to validation values, returning an error if any exception is thrown."
  [f error & args]
  `(catch-exceptions Exception ~f ~error ~@args))

(def check-nil
  "Return an error if the value is nil, otherwise leave it as it is."
  (partial check (complement nil?)))

(defn extract
  "Apply a function to a validation value, returning an error on nil."
  [f error x]
  (->> x (fmap f) (check-nil error)))

(defn default
  "Give a validation value a default value if it's an error."
  [v x]
  (if (errors x) (success v) x))
