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
  (is (= (v/failure "Odd") (v/check even? "Odd" (v/success 1))))
  (is (= (v/success 2) (v/check even? "Odd" (v/success 2))))
  (is (= (v/failure ":-(") (v/check even? "Odd" (v/failure ":-("))))
  (is (= (v/success 1) (v/check-not nil? :whoops (v/success 1))))
  (is (= (v/failure :whoops) (v/check-not nil? :whoops (v/success nil))))
  (is (= (v/failure :yikes) (v/check-not nil? :whoops (v/failure :yikes)))))

(deftest combining
  (is (= (v/success 0)
         (let [x (v/success 0)]
           (-> x
               (v/unless
                 (v/check even? :odd x)
                 (v/check zero? :mag x))))))
  (is (= (v/failure :whoops)
         (let [x (v/failure :whoops)]
           (-> x
               (v/unless
                 (v/check even? :odd x)
                 (v/check zero? :mag x))))))
  (is (= (v/failure :odd :mag)
         (let [x (v/success -1)]
           (-> x (v/unless
                   (v/check even? :odd x)
                   (v/check zero? :mag x)))))))

(deftest trying
  (is (= (v/failure "Couldn't parse.")
         (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (thrown?
        NumberFormatException
        (v/catch-exception NullPointerException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
  (is (= (v/success 8)
         (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "8")))))

(deftest extracting
  (is (= (v/success 3) (v/extract :x :whoops (v/success {:x 3 :y 8}))))
  (is (= (v/success 3) (v/extract first :whoops (v/success [3 4 5]))))
  (is (= (v/failure :yikes) (v/extract :x :whoops (v/failure :yikes))))
  (is (= (v/failure :whoops) (v/extract :z :whoops (v/success {:x 3 :y 8})))))

(deftest defaulting
  (is (= (v/success 5) (v/default 5 (v/failure :whoops))))
  (is (= (v/success 6) (v/default (v/success 5) (v/success 6)))))

(fmapping)
(checking)
(combining)
(trying)
(extracting)
(defaulting)
