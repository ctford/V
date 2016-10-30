(ns V.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [V.core :as v]))

(deftest lifting
  (is (= {:value 3} (v/lift + (v/success 1) (v/success 2))))
  (is (= {:errors #{":-("}} (v/lift + (v/failure ":-(") (v/success 2))))
  (is (= {:errors #{":-("}} (v/lift + (v/success 1) (v/failure ":-("))))
  (is (= {:errors #{":-|" ":-/" ":-("}} (v/lift + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))

(deftest checking
  (is (= {:errors #{"Odd"}} (v/check (v/success 1) even? "Odd")))
  (is (= {:value 2} (v/check (v/success 2) even? "Odd")))
  (is (= {:errors #{":-("}} (v/check (v/failure ":-(") even? "Odd")))
  (is (= {:value 0} (v/check (v/success 0) zero? :non-zero even? :odd)))
  (is (= {:errors #{:non-zero}} (v/check (v/success 4) zero? :non-zero even? :odd)))
  (is (= {:errors #{:non-zero :odd}} (v/check (v/success 3) zero? :non-zero even? :odd ))))

(deftest trying
  (is (= {:errors #{"Couldn't parse."}} (v/exception->error #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (= {:value 8} (v/exception->error #(Integer/parseInt %) "Couldn't parse." (v/success "8")))))

(lifting)
(checking)
(trying)
