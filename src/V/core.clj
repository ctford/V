(ns V.core
  (:require [clojure.set :as set]))

(defn success [x] {:value x})
(def value :value)

(defn failure [& errors] {:errors (set errors)})
(def errors :errors)

(defn all-errors [values]
  (->> values (map errors) (reduce set/union nil)))

(defn lift
  "Apply a function to validate values."
  [f & args]
  (if-let [errors (all-errors args)]
    (apply failure errors)
    (->> args (map value) (apply f) success)))

(defn check
  "Apply a predicate to a validate value, returning the original value if it succeeds or an error if it fails."
  ([x] x)
  ([ok? error & other-checks]
   (let [x (last other-checks)]
     (cond
       (errors x) x
       (-> x value ok?) (apply check other-checks)
       :otherwise (->> other-checks (apply check) errors (apply failure error))))))

(defn catch-exceptions
  "Apply a function to validation values, returning an error if an exception is thrown."
  [f error & args]
  (try
    (apply (partial lift f) args)
    (catch Exception _
      (failure error))))

(def check-nil (partial check (complement nil?)))

(defn extract
  [f error x]
  (->> x (lift f) (check-nil error)))

(defn default [d x]
  (if (errors x) (success d) x))
