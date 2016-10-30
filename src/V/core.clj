(ns V.core
  (:require [clojure.set :as set]))

(defn success [x] {:value x})
(def value :value)

(defn failure [& errors] {:errors (set errors)})
(def errors :errors)

(defn all-errors [values]
  (->> values (map errors) (reduce set/union nil)))

(defn lift
  "Lift a plain function to receive and return validation values."
  [f & args]
  (if-let [errors (all-errors args)]
    (apply failure errors)
    (->> args (map value) (apply f) success)))

(defn check
  "Lift plain predicates to return either the original value or an error."
  ([x] x)
  ([ok? error & other-checks]
   (let [x (last other-checks)]
     (cond
       (errors x) x
       (-> x value ok?) (apply check other-checks)
       :otherwise (->> other-checks (apply check) errors (apply failure error))))))

(defn exception->error
  "Lift a function that might throw exceptions to return errors instead."
  [f error & args]
  (try
    (apply (partial lift f) args)
    (catch Exception _
      (failure error))))

(def nil->error (partial check (complement nil?)))

(defn extract
  [f error x]
  (->> x (lift f) (nil->error error)))

(defn default [d x]
  (if (errors x) (success d) x))
