(ns V.core
  (:require [clojure.set :as set]))

(defn success [x]
  {:value x})

(def value :value)

(defn failure [& errors]
  {:errors (set errors)})

(defn v-apply
  "Apply a function that accepts plain values and returns validation values."
  [f args]
  (if-let [errors (->> args (map :errors) (reduce set/union nil))]
    (apply failure errors)
    (->> args (map :value) (apply f))))

(defn lift
  "Lift a plain function to receive and return validation values."
  [f]
  (fn [& args]
    (v-apply (comp success f) args)))

(defn check
  "Lift a plain predicate to return either the original value or an error."
  [ok? error]
  (fn [x]
    (cond
      (:errors x) x
      (-> x :value ok? not) (failure error)
      :otherwise x)))

(defn all-errors [values]
  (->> values (map :errors) (reduce set/union nil)))

(defn all
  "Turn a seq of checkers into one checker that gathers errors."
  [checks]
  (fn [x]
    (let [check-all (apply juxt checks)
          errors (->> x check-all all-errors)]
      (if errors
        (apply failure errors)
        x))))

(defn checks [ok? error & others]
  (if others
    (all [(check ok? error) (apply checks others)])
    (check ok? error)))

(defn exception->error
  "Lift a function that might throw exceptions to return errors instead."
  [f error]
  (fn [& args]
    (try
      (apply (lift f) args)
      (catch Exception _
        (failure error)))))

(def nil->error (partial check (complement nil?)))
