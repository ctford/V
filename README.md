# V

A Clojure validation library.

## Usage

V works by lifting ordinary functions so that they operate on validation values:

    (is (= (v/success 3)
           (v/fmap + (v/success 1) (v/success 2))))

Errors are aggregated:

    (is (= (v/failure ":-|" ":-/" ":-(")
           (v/fmap + (v/failure ":-/") (v/failure ":-(") (v/failure ":-|"))))))

V can turn functions that throw exceptions into ones that return validation errors:

    (is (= (v/failure "Couldn't parse.")
           (v/catch-any-exception #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))

    (is (= (v/failure "Couldn't parse.")
           (v/catch-exception NumberFormatException #(Integer/parseInt %) "Couldn't parse." (v/success "foo"))))

V also lifts predicates, which will leave the value untouched but potentially return a validation error:

    (is (= (v/failure "Odd")
           (v/check-nil even? "Odd" (v/success 1))))

Errors can be any values - strings, maps etc.

## Running tests

    lein test
