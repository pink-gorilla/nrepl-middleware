(ns gorilla-middleware.render_values-test
  (:require [clojure.test :refer [testing is deftest]]
            [pinkgorilla.middleware.json :as json]))

;; TODO: Only force loading for now
(require 'pinkgorilla.middleware.handle
         'pinkgorilla.middleware.render-values)

(deftest json-serialization
  (testing "Default JSON serialization"
    (is (= (json/serialize {:a 1}) "{\"a\":1}"))))
