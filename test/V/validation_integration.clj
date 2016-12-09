(ns V.validation-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(defn within? [a b k]
  (v/check
    #(and (number? %) (<= a % b))
    [k :bad-date]))

(defn parse-date [m k]
  (let [from (v/fmap -)
        date (v/catch-exception ClassCastException #(java.util.Date. %1 %2 %3) [k :bad-date])
        day?   (within? 1 31 k)
        month? (within? 1 12 k)
        year?  (within? 1900 2017 k)

        day   (-> m (v/extract :day   [k :missing :day])   day?)
        month (-> m (v/extract :month [k :missing :month]) month?)
        year  (-> m (v/extract :year  [k :missing :year])  year?)]
    (date (from year 1900) month day)))

(defn parse-interval [text]
  (let [load-string (v/catch-exception RuntimeException load-string [:json :invalid])
        in-order? (v/check #(.before (first %) (second %)) [:interval :invalid])
        retrieve (fn [m field] (v/extract m field [field :missing]))

        json  (load-string text)
        start (-> json
                  (retrieve :start)
                  (parse-date :start))
        end   (-> json
                  (retrieve :end)
                  (v/default {:day 1 :month 1 :year 2017})
                  (parse-date :end))
        interval (comp in-order? (v/fmap vector))]
    (interval start end)))

(deftest integration

  (testing "Happy path"

    (is (= (parse-interval "{:start {:day 3 :month 2 :year 2016} :end {:day 4 :month 3 :year 2016}}")
           [(java.util.Date. 116 2 3) (java.util.Date. 116 3 4)])))

  (testing "Sad paths"

    (is (= (parse-interval nil)
           (v/failure [:json :invalid])))

    (is (= (parse-interval "asdfasdfasd")
           (v/failure [:json :invalid])))

    (is (= (parse-interval "{:start {:month 2}}")
           (v/failure [:start :missing :day] [:start :missing :year])))

    (is (= (parse-interval "{:start {:day 3 :month \"Foo\" :year 2016}}")
           (v/failure [:start :bad-date])))

    (is (= (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}")
           (v/failure [:interval :invalid])))

    (is (= (parse-interval "{:start {:day -3 :month 2 :year 2016} :end {:day 44 :month 3 :year 2016}}")
          (v/failure [:start :bad-date] [:end :bad-date])))))

(integration)
