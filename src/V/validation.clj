(ns V.validation
  (:require [clojure.set :as set]))

(defn failure
  "Return a failure containing errors."
  [& errors]
  (-> (set errors) (with-meta {::failure true})))

(defn errors
  "Get the errors of a failure, or nil if it's not a failure."
  [x]
  (when (-> x meta ::failure) x))

(defn v-apply
  "Apply a function, collecting the errors of any failures."
  [f args]
  (if-let [combined-errors (->> args (map errors) (reduce set/union nil))]
    (apply failure combined-errors)
    (apply f args)))

(defn fmap
  "Lift an ordinary function to tolerate failures."
  [f]
  (fn [& args]
    (v-apply f args)))

(defn check
  "Lift a predicate, tolerating failures and returning a failure if the predicate fails."
  [ok? error]
  (fn [x]
    (cond
      (errors x) x
      (ok? x) x
      :otherwise (failure error))))

(defn unless
  "Return value unless there are errors from applying the checks."
  [value & checks]
  (v-apply (constantly value) ((apply juxt checks) value)))

(defn extract
  "Apply a function, tolerating failures and turning nil into a failure."
  [x f error]
  (let [f (comp (check (comp not nil?) error) (fmap f))]
    (f x)))

(defn default
  "Substitute a default value if x is a failure."
  [x value]
  (if (errors x) value x))

(defmacro catch-exception
  "Apply a function, tolerating failures and turning exceptions into failures."
  [exception-type f error]
  `(fn [& args#]
     (try
       (v-apply ~f args#)
       (catch ~exception-type _#
         (failure ~error)))))
