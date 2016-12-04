# V

[![Build Status](https://travis-ci.org/ctford/V.png)](https://travis-ci.org/ctford/V)

A Clojure validation library.

## Usage

V works by lifting ordinary functions so that they operate on validation values:

```clojure
(is (= 3
       (+ 1 2)))

(is (= 3
       (v/fmap + 1 2)))
```

The difference with ordinary function application is that the arguments can have errors. If they do, the errors are aggregated:

```clojure
(is (= (v/failure ":-|" ":-/" ":-(")
       (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|"))))))
```

V can turn thrown exceptions into errors:

```clojure
(defn parse-int [s] (Integer/parseInt s))

(is (= (v/failure "Couldn't parse.")
       (v/catch-exception NumberFormatException parse-int "Couldn't parse." "foo")))
```

V also checks validation values using predicates, which leave the value untouched but potentially return errors:

```clojure
(is (= (v/failure "Odd")
       (-> 1
           (unless
             (v/check number? "Non-numeric")
             (v/check even?   "Odd")))))
```

Errors can be any values - strings, maps, tuples etc.

## Running tests

    lein test
