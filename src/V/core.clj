(ns V.core
  (:require [clojure.set :as set]))

(defn success [x]
  {:value x})

(defn failure [& errors]
  {:errors (set errors)})

(def value :value)
(def errors :errors)

(defn all-errors [values]
  (->> values (map errors) (reduce set/union nil)))

(defn v-apply
  [f args]
  (if-let [errors (all-errors args)]
    (apply failure errors)
    (->> args (map value) (apply f))))

(defn lift
  "Lift a plain function to receive and return validation values."
  [f & args]
  (v-apply (comp success f) args))

(defn check*
  [[ok? error]]
  (fn [x]
    (cond
      (errors x) x
      (-> x value ok?) x
      :otherwise (failure error))))

(defn both
  [a b]
  (fn [x]
    (if-let [errors (->> [(a x) (b x)] all-errors)]
      (apply failure errors)
      x)))

(defn check
  "Lift plain predicates to return either the original value or an error."
  [x & xs]
  ((->> xs
        (partition 2)
        (map check*)
        (reduce both identity)) x))

(defn exception->error
  "Lift a function that might throw exceptions to return errors instead."
  [f error & args]
  (try
    (apply (partial lift f) args)
    (catch Exception _
      (failure error))))

(defn nil->error
  [error x]
  (check x (complement nil?) error))

(defn extract
  [f error x]
  (->> x (lift f) (nil->error error)))

(defn default [d x]
  (if (errors x) (success d) x))
