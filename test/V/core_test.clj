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

(defn parse-date [k m]
  (let [day ((v/extract :day [k :missing-day]) m)
        month ((v/extract :month [k :missing-month]) m)
        year ((v/extract :year [k :missing-year]) m)
        adjust (v/lift #(- % 1900))
        date (v/exception->error #(java.util.Date. %1 %2 %3) [k :bad-date])]
    (date (adjust year) month day)))

(defn parse-interval [text]
  (let [value (v/success text)
        json ((v/exception->error load-string [:json :invalid]) value)
        start ((v/extract :start [:start :missing]) json)
        end ((v/extract :end [:end :missing]) json)
        interval ((v/lift list) (parse-date :start start) (parse-date :end end))
        before #(.before (first %) (second %))]
    ((v/check before [:interval :invalid]) interval)))

(deftest integration
  (is (= (v/success [(java.util.Date. 116 2 3) (java.util.Date. 116 3 4)])
         (parse-interval "{:start {:day 3 :month 2 :year 2016} :end {:day 4 :month 3 :year 2016}}")))
  (is (= (v/failure [:json :invalid])
         (parse-interval "asdfasdfasd")))
  (is (= (v/failure [:start :missing-day] [:start :missing-year] [:end :missing])
         (parse-interval "{:start {:month 2}}")))
  (is (= (v/failure [:start :bad-date] [:end :missing])
         (parse-interval "{:start {:day 3 :month \"Foo\" :year 2016}}")))
  (is (= (v/failure [:interval :invalid])
         (parse-interval "{:start {:day 3 :month 4 :year 2016} :end {:day 4 :month 3 :year 2016}}"))))

(lifting)
(checking)
(trying)
(niling)
(integration)
