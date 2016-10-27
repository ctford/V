(ns V.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [V.core :refer :all]))

(deftest lifting
  (is (= {:value 3} ((v-lift +) (success 1) (success 2))))
  (is (= {:errors #{":-("}} ((v-lift +) (failure ":-(") (success 2))))
  (is (= {:errors #{":-("}} ((v-lift +) (success 1) (failure ":-("))))
  (is (= {:errors #{":-|" ":-/" ":-("}} ((v-lift +) (failure ":-/") (failure ":-(") (failure ":-|")))))

(deftest checking
  (is (= {:errors #{"Odd"}} ((v-check even? "Odd") (success 1))))
  (is (= {:value 2} ((v-check even? "Odd") (success 2))))
  (is (= {:errors #{":-("}} ((v-check even? "Odd") (failure ":-(")))))

(deftest trying
  (is (= {:errors #{"Couldn't parse."}} ((v-try #(Integer/parseInt %) "Couldn't parse.") (success "foo"))))
  (is (= {:value 8} ((v-try #(Integer/parseInt %) "Couldn't parse.") (success "8")))))

(deftest niling
  (is (= {:errors #{"Nil!"}} ((v-nil? "Nil!") (success nil))))
  (is (= {:value 17} ((v-nil? "Nil!") (success 17))))
  (is (= {:errors #{":-("}} ((v-nil? "Nil!") (failure ":-(")))))

(lifting)
(checking)
(trying)
(niling)
