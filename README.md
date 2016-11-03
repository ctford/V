# V

[![Build Status](https://travis-ci.org/ctford/V.png)](https://travis-ci.org/ctford/V)

A Clojure validation library.

## Usage

V works by lifting ordinary functions so that they operate on validation values:

```clojure
(is (= (v/success 3)
       (v/fmap + (v/success 1) (v/success 2))))
```

Errors are aggregated:

```clojure
(is (= (v/failure ":-|" ":-/" ":-(")
       (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|"))))))
```

V can turn functions that throw exceptions into ones that return validation errors:

```clojure
(is (= (v/failure "Couldn't parse.")
       (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))
```

V also lifts predicates, which will leave the value untouched but potentially return a validation error:

```clojure
(is (= (v/failure "Odd")
       (-> (v/success 1)
           (v/check number? "Non-numeric"
                    even?   "Odd")))
```

Errors can be any values - strings, maps etc.

## Running tests

    lein test
