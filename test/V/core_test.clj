(ns V.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [V.core :refer :all]))

(deftest lifting
  (is (= {:value 3} ((v-lift +) (success 1) (success 2))))
  (is (= {:errors #{":-("}} ((v-lift +) (failure ":-(") (success 2))))
  (is (= {:errors #{":-("}} ((v-lift +) (success 1) (failure ":-("))))
  (is (= {:errors #{":-|" ":-/" ":-("}} ((v-lift +) (failure ":-/") (failure ":-(") (failure ":-|")))))

(lifting)
