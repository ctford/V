(ns V.validation-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [V.validation :as v]))

(deftest mapping
  (let [+ (v/fmap +)]
    (is (= 3 (+ 1 2)))
    (is (= (v/failure ":-(") (+ (v/failure ":-(") 2)))
    (is (= (v/failure ":-(") (+ 1 (v/failure ":-("))))
    (is (= (v/failure ":-|" ":-/" ":-(") (+ (v/failure ":-/") (v/failure ":-(") (v/failure ":-|"))))) )

(deftest checking
  (let [even? (v/check even?)
        zero? (v/check zero?)]
    (testing "Positive checks"
      (is (= (v/failure "Odd") ((even? "Odd") 1)))
      (is (= 2 ((even? "Odd") 2)))
      (is (= (v/failure ":-(") ((even? "Odd") (v/failure ":-(")))))
    (testing "Multiple positive checks"
      (is (= (v/failure "Odd" "Non-zero") (-> 1 (v/unless (even? "Odd") (zero? "Non-zero")))))
      (is (= 0 (-> 0 (v/unless (even? "Odd") (zero? "Non-zero")))))
      (is (= (v/failure ":-(") (-> (v/failure ":-(") (v/unless (even? "Odd") (zero? "Non-zero"))))))))

(defn parse-int [s] (Integer/parseInt s))

(deftest trying
  (let [parse-int (v/catch-exception NumberFormatException parse-int)]
    (is (= (v/failure "Couldn't parse.") (parse-int "Couldn't parse." "foo")))
    (is (= 8 (parse-int "Couldn't parse." "8"))))
  (let [parse-int (v/catch-exception NullPointerException parse-int)]
    (is (thrown? NumberFormatException (parse-int "Couldn't parse." "foo")))))

(deftest extracting
  (is (= 3 (-> {:x 3 :y 8} (v/extract :x :whoops))))
  (is (= 3 (-> [3 4 5] (v/extract first :whoops))))
  (is (= (v/failure :yikes) (-> (v/failure :yikes) (v/extract :x :whoops))))
  (is (= (v/failure :whoops) (-> {:x 3 :y 8} (v/extract :z :whoops)))))

(deftest defaulting
  (is (= 5 (-> (v/failure :whoops) (v/default 5))))
  (is (= 6 (-> 6 (v/default 5)))))

(mapping)
(checking)
(trying)
(extracting)
(defaulting)
