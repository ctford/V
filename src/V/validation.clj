(ns V.validation
  (:require [clojure.set :as set]))

(defn failure
  "Return a failure."
  [& errors]
  (-> (set errors) (with-meta {::failure true})))

(defn errors
  "Get the errors of a lifted value, or nil if it's a success."
  [x]
  (when (-> x meta ::failure) x))

(defn v-apply
  "Apply an ordinary function to lifted arguments, collecting any errors."
  [f args]
  (if-let [combined-errors (->> args (map errors) (reduce set/union nil))]
    (apply failure combined-errors)
    (apply f args)))

(defn fmap
  "Lift an ordinary function to accept lifted arguments and return a successful result."
  [f]
  (fn [& args] (v-apply f args)))

(defn check
  "Lift a predicate to take a validation value, returning either the original value or an error."
  [ok? error]
  (fn [x]
    (cond
      (errors x) x
      (ok? x) x
      :otherwise (failure error))))

(defn unless
  "Return v unless there are errors from applying the checks."
  [v & checks]
  (v-apply (constantly v) ((apply juxt checks) v)))

(defn extract
  "Apply a function to a lifted value, returning an error on nil."
  [x f error]
  (let [f (comp (check (comp not nil?) error) (fmap f))]
    (f x)))

(defn default
  "Turn a lifted value into a success if it's a failure."
  [x v]
  (if (errors x) v x))

(defmacro catch-exception
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type f error]
  `(fn [& args#]
     (try
       (apply (fmap ~f) args#)
       (catch ~exception-type _#
         (failure ~error)))))
