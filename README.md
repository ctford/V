# V

A Clojure validation library.

## Usage

V works by lifting ordinary functions so that they operate on validation values:

    (is (= {:value 3} ((v-lift +) (success 1) (success 2))))

Failures are aggregated:

    (is (= {:errors #{":-|" ":-/" ":-("}} ((v-lift +) (failure ":-/") (failure ":-(") (failure ":-|")))))

V can turn functions that throw exceptions into ones that return validation errors:

    (is (= {:errors #{"Couldn't parse."}} ((v-try #(Integer/parseInt %) "Couldn't parse.") (success "foo"))))

We can also lift predicates, which will leave the value untouched but potentially return a validation error:

    (is (= {:errors #{"Odd"}} ((v-check even? "Odd") (success 1))))

Errors can be any values - strings, maps etc.
