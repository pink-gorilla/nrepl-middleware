(ns pinkgorilla.middleware.render_values-test
  (:require 
   [clojure.test :refer :all]
   [pinkgorilla.middleware.json :as json]
   [pinkgorilla.middleware.render-values :refer :all]))

(deftest json-serialization
  (testing "Default JSON serialization"
    (is (= (json/serialize {:a 1}) "{\"a\":1}"))))
