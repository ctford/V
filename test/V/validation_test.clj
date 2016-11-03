(ns V.validation-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(deftest fmapping
  (is (= (v/success 3) (v/fmap + (v/success 1) (v/success 2))))
  (is (= (v/failure ":-(") (v/fmap + (v/failure ":-(") (v/success 2))))
  (is (= (v/failure ":-(") (v/fmap + (v/success 1) (v/failure ":-("))))
  (is (= (v/failure ":-|" ":-/" ":-(") (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))

(deftest checking
  (testing "Positive checks"
    (is (= (v/failure "Odd") (-> (v/success 1) (v/check even? "Odd"))))
    (is (= (v/success 2) (-> (v/success 2) (v/check even? "Odd"))))
    (is (= (v/failure ":-(") (-> (v/failure ":-(") (v/check even? "Odd")))))
  (testing "Multiple positive checks"
    (is (= (v/failure "Odd" "Non-zero") (-> (v/success 1) (v/check even? "Odd" zero? "Non-zero"))))
    (is (= (v/success 0) (-> (v/success 0) (v/check even? "Odd" zero? "Non-zero"))))
    (is (= (v/failure ":-(") (-> (v/failure ":-(") (v/check even? "Odd" zero? "Non-zero")))))
  (testing "Negative checks"
    (is (= (v/success 1) (-> (v/success 1) (v/check-not nil? :whoops))))
    (is (= (v/failure :whoops) (-> (v/success nil) (v/check-not nil? :whoops))))
    (is (= (v/failure :yikes) (-> (v/failure :yikes) (v/check-not nil? :whoops)))))
  (testing "Multiple negative checks"
    (is (= (v/success 1) (-> (v/success 1) (v/check-not nil? :whoops (partial = 2)))))
    (is (= (v/failure :whoops) (-> (v/success 0) (v/check-not zero? :whoops (partial = 1) :yikes))))
    (is (= (v/failure :yikes) (-> (v/failure :yikes) (v/check-not nil? :whoops (partial = 1) :ouch))))
    (is (= (v/failure :whoops :ouch) (-> (v/success 0) (v/check-not even? :whoops zero? :ouch))))))

(deftest trying
  (is (= (v/failure "Couldn't parse.")
         (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (thrown?
        NumberFormatException
        (v/catch-exception NullPointerException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (= (v/success 8)
         (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "8")))))

(deftest extracting
  (is (= (v/success 3) (-> (v/success {:x 3 :y 8}) (v/extract :x :whoops))))
  (is (= (v/success 3) (-> (v/success [3 4 5]) (v/extract first :whoops))))
  (is (= (v/failure :yikes) (-> (v/failure :yikes) (v/extract :x :whoops))))
  (is (= (v/failure :whoops) (-> (v/success {:x 3 :y 8}) (v/extract :z :whoops)))))

(deftest defaulting
  (is (= (v/success 5) (-> (v/failure :whoops) (v/default 5))))
  (is (= (v/success 6) (-> (v/success 6) (v/default 5)))))

(fmapping)
(checking)
(trying)
(extracting)
(defaulting)
