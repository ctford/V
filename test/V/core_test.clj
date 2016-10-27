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

(lifting)
(checking)
(trying)
(niling)
