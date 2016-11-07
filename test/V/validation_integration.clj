(ns V.validation-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(defn date [k y m d]
  (v/catch-exception
    ClassCastException
    #(java.util.Date. %1 %2 %3)
    [k :bad-date]
    y m d))

(defn within? [a b]
  (fn [x] (and (number? x) (<= a x b))))

(defn parse-date [m k]
  (v/with-lift v/lift [-]
    (let [day   (-> m (v/extract :day                [k :missing-day])
                      (v/check   (within? 1 31)      [k :bad-date]))
          month (-> m (v/extract :month              [k :missing-month])
                      (v/check   (within? 1 12)      [k :bad-date]))
          year  (-> m (v/extract :year               [k :missing-year])
                      (v/check   (within? 1900 2017) [k :bad-year]))]
      (date k (- year (v/success 1900)) month day))))

(defn parse-interval [text]
  (v/with-lift v/lift [vector]
    (v/with-lift v/success [text]
      (v/with-lift (v/catch-exception* RuntimeException) [load-string]
        (let [json  (load-string [:json :invalid] text)
              start (-> json
                        (v/extract  :start [:start :missing])
                        (parse-date :start))
              end   (-> json
                        (v/extract  :end [:end :missing])
                        (v/default {:day 1 :month 1 :year 2017})
                        (parse-date :end))]
          (-> (vector start end)
              (v/check #(.before (first %) (second %)) [:interval :invalid])))))))

(deftest integration
  (testing "Happy path"
    (is (= (v/success [(java.util.Date. 116 2 3) (java.util.Date. 116 3 4)])
           (parse-interval "{:start {:day 3 :month 2 :year 2016} :end {:day 4 :month 3 :year 2016}}"))))
  (testing "Sad paths"
    (is (= (v/failure [:json :invalid])
           (parse-interval "asdfasdfasd")))
    (is (= (v/failure [:start :missing-day] [:start :missing-year])
           (parse-interval "{:start {:month 2}}")))
    (is (= (v/failure [:start :bad-date])
           (parse-interval "{:start {:day 3 :month \"Foo\" :year 2016}}")))
    (is (= (v/failure [:interval :invalid])
           (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}")))
    (is (= (v/failure [:start :bad-date] [:end :bad-date])
           (parse-interval "{:start {:day -3 :month 2 :year 2016} :end {:day 44 :month 3 :year 2016}}")))))

(integration)
