(ns V.validation-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(deftest mapping
  (testing "Fmapping"
    (is (= (v/success 3) (v/fmap + (v/success 1) (v/success 2))))
    (is (= (v/failure ":-(") (v/fmap + (v/failure ":-(") (v/success 2))))
    (is (= (v/failure ":-(") (v/fmap + (v/success 1) (v/failure ":-("))))
    (is (= (v/failure ":-|" ":-/" ":-(") (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))
  (testing "Lifting"
    (v/with-lift v/lift [+]
      (is (= (v/success 3) (+ (v/success 1) (v/success 2))))
      (is (= (v/failure ":-(") (+ (v/failure ":-(") (v/success 2))))
      (is (= (v/failure ":-(") (+ (v/success 1) (v/failure ":-("))))
      (is (= (v/failure ":-|" ":-/" ":-(") (+ (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))) )

(deftest checking
  (testing "Positive checks"
    (v/with-lift v/check* [even?]
      (is (= (v/failure "Odd") (-> (v/success 1) (even? "Odd"))))
      (is (= (v/success 2) (-> (v/success 2) (even? "Odd"))))
      (is (= (v/failure ":-(") (-> (v/failure ":-(") (even? "Odd"))))))
  (testing "Multiple positive checks"
    (v/with-lift v/check* [even? zero?]
      (let [x (v/success 1)
            y (v/success 0)
            z (v/failure ":-(")]
        (is (= (v/failure "Odd" "Non-zero")
               (v/unless x (even? x "Odd") (zero? x "Non-zero"))))
        (is (= (v/success 0) (v/unless y (even? y "Odd") (zero? y "Non-zero"))))
        (is (= (v/failure ":-(") (v/unless z (even? z "Odd") (zero? z "Non-zero"))))))))

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

(mapping)
(checking)
(trying)
(extracting)
(defaulting)
