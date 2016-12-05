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
  (let [even? (v/check even? "Odd")
        zero? (v/check zero? "Non-zero")]
    (testing "Positive checks"
      (is (= (v/failure "Odd") (even? 1)))
      (is (= 2 (even? 2)))
      (is (= (v/failure ":-(") (even? (v/failure ":-(")))))
    (testing "Multiple positive checks"
      (is (= (v/failure "Odd" "Non-zero") (-> 1 (v/unless even? zero?))))
      (is (= 0 (-> 0 (v/unless even? zero?))))
      (is (= (v/failure ":-(") (-> (v/failure ":-(") (v/unless even? zero?)))))))

(defn parse-int [s] (Integer/parseInt s))

(deftest trying
  (let [parse-int (v/catch-exception NumberFormatException parse-int "Couldn't parse.")]
    (is (= (v/failure "Couldn't parse.") (parse-int "foo")))
    (is (= 8 (parse-int "8"))))
  (let [parse-int (v/catch-exception NullPointerException parse-int "Couldn't parse.")]
    (is (thrown? NumberFormatException (parse-int "foo")))))

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
