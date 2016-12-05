(ns V.validation-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(defn within? [a b]
  (fn [x] (and (number? x) (<= a x b))))

(defn parse-date [m k]
  (let [- (v/fmap -)
        date (v/catch-exception ClassCastException #(java.util.Date. %1 %2 %3) [k :bad-date])
        day?   (v/check (within? 1 31)      [k :bad-date])
        month? (v/check (within? 1 12)      [k :bad-date])
        year?  (v/check (within? 1900 2017) [k :bad-year])
        get (fn [m field] (v/extract m field [k :missing field]))
        day    (-> m (get :day) day?)
        month  (-> m (get :month) month?)
        year   (-> m (get :year) year?)]
    (date (- year 1900) month day)))

(defn parse-interval [text]
  (let [vector (v/fmap vector)
        load-string (v/catch-exception RuntimeException load-string [:json :invalid])
        in-order? (v/check #(.before (first %) (second %)) [:interval :invalid])
        get (fn [m field] (v/extract m field [field :missing]))
        json  (load-string text)
        start (-> json
                  (get :start)
                  (parse-date :start))
        end   (-> json
                  (get :end)
                  (v/default {:day 1 :month 1 :year 2017})
                  (parse-date :end))]
    (-> (vector start end) in-order?)))

(deftest integration
  (testing "Happy path"
    (is (= [(java.util.Date. 116 2 3) (java.util.Date. 116 3 4)]
           (parse-interval "{:start {:day 3 :month 2 :year 2016} :end {:day 4 :month 3 :year 2016}}"))))
  (testing "Sad paths"
    (is (= (v/failure [:json :invalid])
           (parse-interval "asdfasdfasd")))
    (is (= (v/failure [:start :missing :day] [:start :missing :year])
           (parse-interval "{:start {:month 2}}")))
    (is (= (v/failure [:start :bad-date])
           (parse-interval "{:start {:day 3 :month \"Foo\" :year 2016}}")))
    (is (= (v/failure [:interval :invalid])
           (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}")))
    (is (= (v/failure [:start :bad-date] [:end :bad-date])
           (parse-interval "{:start {:day -3 :month 2 :year 2016} :end {:day 44 :month 3 :year 2016}}")))))

(integration)
