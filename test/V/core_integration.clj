(ns V.core-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.core :as v]))

(defn parse-date [m k]
  (let [day (->> m (v/extract :day [k :missing-day]))
        month (->> m (v/extract :month [k :missing-month]))
        year (->> m (v/extract :year [k :missing-year]) (v/fmap #(- % 1900)))
        date (fn [y m d] (v/catch-all-exceptions #(java.util.Date. %1 %2 %3) [k :bad-date] y m d))]
    (date year month day)))

(defn parse-interval [text]
  (let [json (->> text v/success (v/catch-all-exceptions load-string [:json :invalid]))
        start-map (->> json (v/extract :start [:start :missing]))
        end-map (->> json (v/extract :end [:end :missing]) (v/default {:day 1 :month 1 :year 2017}))
        interval (fn [s e]
                   (->> (v/fmap vector s e)
                        (v/check #(.before (first %) (second %)) [:interval :invalid])))]
    (interval (parse-date start-map :start) (parse-date end-map :end))))

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
