(ns V.validation-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(defn date [y m d]
  (java.util.Date. y m d))

(defn parse-date [k m]
  (let [day   (->> m (v/extract :day   [k :missing-day]))
        month (->> m (v/extract :month [k :missing-month]))
        year  (->> m (v/extract :year  [k :missing-year]) (v/fmap #(- % 1900)))]
    (v/catch-exception ClassCastException date [k :bad-date] year month day)))

(defn parse-interval [text]
  (let [json  (->> (v/success text)
                   (v/catch-exception RuntimeException load-string [:json :invalid]))
        start (->> json
                   (v/extract :start [:start :missing])
                   (parse-date :start))
        end   (->> json
                   (v/extract :end [:end :missing])
                   (v/default {:day 1 :month 1 :year 2017})
                   (parse-date :start))]
    (->> (v/fmap vector start end)
         (v/check #(.before (first %) (second %)) [:interval :invalid]))))

(deftest integration
  (testing "Happy path"
    (is (= (v/success [(date 116 2 3) (date 116 3 4)])
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
