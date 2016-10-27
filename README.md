# V

A Clojure validation library.

## Usage

V works by lifting ordinary functions so that they operate on validation values:

    (is (= {:value 3} ((v/lift +) (v/success 1) (v/success 2))))

Errors are aggregated:

    (is (= {:errors #{":-|" ":-/" ":-("}} ((v/lift +) (v/failure ":-/") (v/failure ":-(") (v/failure ":-|")))))

V can turn functions that throw exceptions into ones that return validation errors:

    (is (= {:errors #{"Couldn't parse."}} ((v/exception->error #(Integer/parseInt %) "Couldn't parse.") (v/success "foo"))))

V also lifts predicates, which will leave the value untouched but potentially return a validation error:

    (is (= {:errors #{"Odd"}} ((v/nil->error even? "Odd") (v/success 1))))

Errors can be any values - strings, maps etc.

## Running tests

    lein test
