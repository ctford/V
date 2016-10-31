(ns V.core-test
  (:require
    [clojure.test :refer [deftest testing is are]]
    [V.core :as v]))

(deftest fmapping
  (is (= (v/success 3) (v/fmap + (v/success 1) (v/success 2))))
  (is (= (v/failure ":-(") (v/fmap + (v/failure ":-(") (v/success 2))))
  (is (= (v/failure ":-(") (v/fmap + (v/success 1) (v/failure ":-("))))
  (is (= (v/failure ":-|" ":-/" ":-(") (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))

(deftest checking
  (is (= (v/failure "Odd") (v/check even? "Odd" (v/success 1))))
  (is (= (v/success 2) (v/check even? "Odd" (v/success 2))))
  (is (= (v/failure ":-(") (v/check even? "Odd" (v/failure ":-("))))
  (is (= (v/success 0) (v/check zero? :non-zero even? :odd (v/success 0))))
  (is (= (v/failure :non-zero) (v/check zero? :non-zero even? :odd (v/success 4))))
  (is (= (v/failure :non-zero :odd) (v/check zero? :non-zero even? :odd (v/success 3)))))

(deftest trying
  (is (= (v/failure "Couldn't parse.") (v/catch-exceptions #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (= (v/success 8) (v/catch-exceptions #(Integer/parseInt %) "Couldn't parse." (v/success "8")))))

(deftest defaulting
  (is (= (v/success 5) (v/default (v/success 5) (v/failure :whoops))))
  (is (= (v/success 6) (v/default (v/success 5) (v/success 6)))))

(fmapping)
(checking)
(trying)
