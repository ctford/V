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
  (is (= (v/failure "Odd") (->> (v/success 1) (v/check even? "Odd"))))
  (is (= (v/success 2) (->> (v/success 2) (v/check even? "Odd"))))
  (is (= (v/failure ":-(") (->> (v/failure ":-(") (v/check even? "Odd"))))
  (is (= (v/success 1) (->> (v/success 1) (v/check-not nil? :whoops))))
  (is (= (v/failure :whoops) (->> (v/success nil) (v/check-not nil? :whoops))))
  (is (= (v/failure :yikes) (->> (v/failure :yikes) (v/check-not nil? :whoops)))))

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
  (is (= (v/success 3) (->> (v/success {:x 3 :y 8}) (v/extract :x :whoops))))
  (is (= (v/success 3) (->> (v/success [3 4 5]) (v/extract first :whoops))))
  (is (= (v/failure :yikes) (->> (v/failure :yikes) (v/extract :x :whoops))))
  (is (= (v/failure :whoops) (->> (v/success {:x 3 :y 8}) (v/extract :z :whoops)))))

(deftest defaulting
  (is (= (v/success 5) (->> (v/failure :whoops) (v/default 5))))
  (is (= (v/success 6) (->> (v/success 6) (v/default 5)))))

(fmapping)
(checking)
(combining)
(trying)
(extracting)
(defaulting)
