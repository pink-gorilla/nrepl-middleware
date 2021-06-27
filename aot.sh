#!/bin/sh

rm -rf classes
mkdir classes

clojure -A:aot -e "(compile 'pinkgorilla.nrepl.aot)"

#  :uberjar-exclusions [#"cider/nrepl.*\.class$"]