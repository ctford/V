(ns V.core
  (:require [clojure.set :as set]))

(defn success [x] {:value x})
(def value :value)

(defn failure [& errors] {:errors (set errors)})
(def errors :errors)

(defn lift
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

(defn catch-exceptions
  "Apply a function to validation values, returning an error if an exception is thrown."
  [f error & args]
  (try
    (apply (partial lift f) args)
    (catch Exception _
      (failure error))))

(def check-nil
  "Return an error if the value is nil, otherwise leave it as it is."
  (partial check (complement nil?)))

(defn extract
  "Apply a function to a validation value, returning an error on nil."
  [f error x]
  (->> x (lift f) (check-nil error)))

(defn default
  "Give a validation value a default value if it's an error."
  [v x]
  (if (errors x) (success v) x))
