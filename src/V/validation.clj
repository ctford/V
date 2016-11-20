(ns V.validation
  (:require [clojure.set :as set]))

(defn success
  "Return a success."
  [x]
  [:value x])

(defn value
  "Get the value of a lifted value, or nil if it's a failure."
  [[k v]]
  (when (= k :value) v))

(defn failure
  "Return a failure."
  [& errors]
  [:errors (set errors)])

(defn errors
  "Get the errors of a lifted value, or nil if it's a success."
  [[k v]]
  (when (= k :errors) v))

(defn v-apply
  "Apply an ordinary function to lifted arguments."
  [f args]
  (if-let [combined-errors (->> args (map errors) (reduce set/union nil))]
    (apply failure combined-errors)
    (->> args (map value) (apply f))))

(defn fmap
  "Lift an ordinary function to accept lifted arguments and return a successful result."
  [f]
  (fn [& args]
    (v-apply (comp success f) args)))

(defn lift-let [syms]
  (mapcat
    (fn [[f xs]] (interleave xs (map (partial list f) xs)))
    (partition 2 syms)))

(defmacro lift
  "Shadow bindings with lifted versions of themselves within a lexical scope."
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
  (-> ((fmap f) x) (check (comp not nil?) error)))

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
         (apply (fmap f#) args#)
         (catch ~exception-type _#
           (failure error#))))))

(defmacro catch-exception
  "Apply a function to validation values, returning an error if a specified exception is thrown."
  [exception-type f error & args]
  `(((catch-exception* ~exception-type) ~f) ~error ~@args))
