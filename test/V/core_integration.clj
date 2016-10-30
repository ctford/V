(ns V.core-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.core :as v]))

(defn parse-date [m k]
  (let [day (v/extract :day [k :missing-day] m)
        month (v/extract :month [k :missing-month] m)
        year (v/extract :year [k :missing-year] m)
        adjust (partial v/lift #(- % 1900))
        date (partial v/exception->error #(java.util.Date. %1 %2 %3) [k :bad-date])]
    (date (adjust year) month day)))

(defn parse-interval [text]
  (let [value (v/success text)
        json (v/exception->error load-string [:json :invalid] value)
        start (v/extract :start [:start :missing] json)
        end (->> json (v/extract :end [:end :missing]) (v/default {:day 1 :month 1 :year 2017}))
        interval (v/lift list (parse-date start :start) (parse-date end :end))
        starts-before-ends #(.before (first %) (second %))]
    (v/check starts-before-ends [:interval :invalid] interval)))

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
           (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}")))))

(integration)
