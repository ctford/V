(ns V.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [V.core :as v]))

(deftest lifting
  (is (= {:value 3} ((v/lift +) (v/success 1) (v/success 2))))
  (is (= {:errors #{":-("}} ((v/lift +) (v/failure ":-(") (v/success 2))))
  (is (= {:errors #{":-("}} ((v/lift +) (v/success 1) (v/failure ":-("))))
  (is (= {:errors #{":-|" ":-/" ":-("}} ((v/lift +) (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))

(deftest checking
  (is (= {:errors #{"Odd"}} ((v/check even? "Odd") (v/success 1))))
  (is (= {:value 2} ((v/check even? "Odd") (v/success 2))))
  (is (= {:errors #{":-("}} ((v/check even? "Odd") (v/failure ":-("))))
  (is (= {:value 0} ((v/check zero? :non-zero even? :odd) (v/success 0))))
  (is (= {:errors #{:non-zero}} ((v/check zero? :non-zero even? :odd) (v/success 4))))
  (is (= {:errors #{:non-zero :odd}} ((v/check zero? :non-zero even? :odd) (v/success 3)))))

(deftest trying
  (is (= {:errors #{"Couldn't parse."}} ((v/exception->error #(Integer/parseInt %) "Couldn't parse.") (v/success "foo"))))
  (is (= {:value 8} ((v/exception->error #(Integer/parseInt %) "Couldn't parse.") (v/success "8")))))

(deftest niling
  (is (= {:errors #{"Nil!"}} ((v/nil->error "Nil!") (v/success nil))))
  (is (= {:value 17} ((v/nil->error "Nil!") (v/success 17))))
  (is (= {:errors #{":-("}} ((v/nil->error "Nil!") (v/failure ":-(")))))

(defn parse-date [m]
  (let [day ((v/extract :day :missing-day) m)
        month ((v/extract :month :missing-month) m)
        year ((v/extract :year :missing-year) m)
        adjust (v/lift #(- % 1900))
        date (v/exception->error #(java.util.Date. %1 %2 %3) :bad-date)]
    (date (adjust year) month day)))

(defn parse-interval [text]
  (let [value (v/success text)
        json ((v/lift load-string) value)
        start ((v/extract :start :missing-start) json)]
    (parse-date start)))

(deftest integration
  (is (= {:value (java.util.Date. 116 2 3)} (parse-interval "{:start {:day 3 :month 2 :year 2016}}")))
  (is (= {:errors #{:missing-day :missing-year}} (parse-interval "{:start {:month 2}}"))))

(lifting)
(checking)
(trying)
(niling)
(integration)
