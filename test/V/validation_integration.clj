(ns V.validation-integration
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(defn date [y m d]
  (java.util.Date. y m d))

(defn between? [a b]
  (fn [x] (and (number? x) (<= a x b))))

(defn parse-date [k m]
  (let [day   (->> m (v/extract :day           [k :missing-day])
                     (v/check (between? 1 32)  [k :bad-date]))
        month (->> m (v/extract :month         [k :missing-month])
                     (v/check (between? 1 13)  [k :bad-date]))
        year  (->> m (v/extract :year          [k :missing-year])
                     (v/fmap #(- % 1900))
                     (v/check (between? 1 117) [k :bad-date]))]
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
                   (parse-date :end))]
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
           (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}")))
    (is (= (v/failure [:start :bad-date] [:end :bad-date])
           (parse-interval "{:start {:day -3 :month 2 :year 2016} :end {:day 44 :month 3 :year 2016}}")))))

(integration)
