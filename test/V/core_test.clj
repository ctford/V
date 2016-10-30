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
  (is (= {:errors #{":-("}} ((v/check even? "Odd") (v/failure ":-(")))))

(deftest trying
  (is (= {:errors #{"Couldn't parse."}} ((v/exception->error #(Integer/parseInt %) "Couldn't parse.") (v/success "foo"))))
  (is (= {:value 8} ((v/exception->error #(Integer/parseInt %) "Couldn't parse.") (v/success "8")))))

(deftest niling
  (is (= {:errors #{"Nil!"}} ((v/nil->error "Nil!") (v/success nil))))
  (is (= {:value 17} ((v/nil->error "Nil!") (v/success 17))))
  (is (= {:errors #{":-("}} ((v/nil->error "Nil!") (v/failure ":-(")))))

(deftest alling
  (is (= {:value 0} ((v/all [(v/check zero? :non-zero) (v/check even? :odd)]) (v/success 0))))
  (is (= {:errors #{:non-zero}} ((v/all [(v/check zero? :non-zero) (v/check even? :odd)]) (v/success 4))))
  (is (= {:errors #{:non-zero :odd}} ((v/all [(v/check zero? :non-zero) (v/check even? :odd)]) (v/success 3)))))

(defn parse-interval [text]
  (let [value (v/success text)
        json ((v/lift load-string) value)
        mandatory (v/all
                    [(v/check (comp (complement nil?) :day) :missing-day)
                     (v/check (comp (complement nil?) :month) :missing-month)
                     (v/check (comp (complement nil?) :year) :missing-year)])
        checked (mandatory json)
        result checked]
    result))

(deftest integration
  (is (= {:value {:day 2 :month 2 :year 2016}} (parse-interval "{:day 2 :month 2 :year 2016}")))
  (is (= {:errors #{:missing-day :missing-year}} (parse-interval "{:month 2}"))))

(lifting)
(checking)
(trying)
(niling)
(alling)
(integration)
