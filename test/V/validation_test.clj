(ns V.validation-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(deftest mapping
  (v/lift [v/fmap* [+]]
    (is (= (v/success 3) (+ (v/success 1) (v/success 2))))
    (is (= (v/failure ":-(") (+ (v/failure ":-(") (v/success 2))))
    (is (= (v/failure ":-(") (+ (v/success 1) (v/failure ":-("))))
    (is (= (v/failure ":-|" ":-/" ":-(") (+ (v/failure ":-/") (v/failure ":-(") (v/failure ":-|"))))) )

(deftest checking
  (v/lift [v/check* [even? zero?]]
    (testing "Positive checks"
      (is (= (v/failure "Odd") ((even? "Odd") (v/success 1))))
      (is (= (v/success 2) ((even? "Odd") (v/success 2))))
      (is (= (v/failure ":-(") ((even? "Odd") (v/failure ":-(")))))
    (testing "Multiple positive checks"
      (is (= (v/failure "Odd" "Non-zero") (-> (v/success 1) (v/unless (even? "Odd") (zero? "Non-zero")))))
      (is (= (v/success 0) (-> (v/success 0) (v/unless (even? "Odd") (zero? "Non-zero")))))
      (is (= (v/failure ":-(") (-> (v/failure ":-(") (v/unless (even? "Odd") (zero? "Non-zero"))))))))

(defn parse-int [s] (Integer/parseInt s))

(deftest trying
  (v/lift [(v/catch-exception* NumberFormatException) [parse-int]]
    (is (= (v/failure "Couldn't parse.") (parse-int "Couldn't parse." (v/success "foo"))))
    (is (= (v/success 8) (parse-int "Couldn't parse." (v/success "8")))))
  (v/lift [(v/catch-exception* NullPointerException) [parse-int]]
    (is (thrown? NumberFormatException (parse-int "Couldn't parse." (v/success "foo"))))))

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
