(ns gorilla-middleware.render_values-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.middleware.formatter :refer [serialize]]))

;; TODO: Only force loading for now
(require 'pinkgorilla.middleware.handle
         'pinkgorilla.middleware.render-values)

(deftest edn-serialization
  (testing "Default EDN serialization"
    (is (= (serialize {:a 1}) "{:a 1}"))
    (is (= (serialize ^:R {:a 1}) "^{:R true} {:a 1}"))))
