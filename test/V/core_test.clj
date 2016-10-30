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
  (is (= {:errors #{"Odd"}} (v/check even? "Odd" (v/success 1))))
  (is (= {:value 2} (v/check even? "Odd" (v/success 2))))
  (is (= {:errors #{":-("}} (v/check even? "Odd" (v/failure ":-("))))
  (is (= {:value 0} (v/check zero? :non-zero even? :odd (v/success 0))))
  (is (= {:errors #{:non-zero}} (v/check zero? :non-zero even? :odd (v/success 4))))
  (is (= {:errors #{:non-zero :odd}} (v/check zero? :non-zero even? :odd (v/success 3)))))

(deftest trying
  (is (= {:errors #{"Couldn't parse."}} (v/exception->error #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (= {:value 8} (v/exception->error #(Integer/parseInt %) "Couldn't parse." (v/success "8")))))

(lifting)
(checking)
(trying)
