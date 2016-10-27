(ns V.core
  (:require [clojure.set :as set]))

(defn success [x]
  {:value x})

(defn failure [& errors]
  {:errors (set errors)})

(defn v-apply
  "Apply a function that accepts plain values and returns validation values."
  [f args]
  (if-let [errors (->> args (map :errors) (reduce set/union nil))]
    (apply failure errors)
    (->> args (map :value) (apply f))))

(defn v-lift
  "Lift a plain function to receive and return validation values."
  [f]
  (fn [& args]
    (v-apply (comp success f) args)))

(defn v-check
  "Lift a plain predicate to return either the original value or an error."
  [ok? error]
  (fn [x]
    (cond
      (:errors x) x
      (-> x :value ok? not) (failure error)
      :otherwise x)))

(defn v-try
  "Lift a function that might throw exceptions to return errors instead."
  [f error]
  (fn [& args]
    (try
      (apply (v-lift f) args)
      (catch Exception _
        (failure error)))))

(def v-nil? (partial v-check (complement nil?)))

