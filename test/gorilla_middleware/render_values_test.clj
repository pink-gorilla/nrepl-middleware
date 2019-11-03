(ns gorilla-middleware.render_values-test
  (:require [clojure.test :refer :all]
    ;; hack to test just compilation for now
            [pinkgorilla.middleware.handle :refer [nrepl-handler]]
            [pinkgorilla.middleware.json :as json]
            [pinkgorilla.middleware.render-values :refer :all]))

(deftest json-serialization
  (testing "Default JSON serialization"
    (is (= (json/serialize {:a 1}) "{\"a\":1}"))))
