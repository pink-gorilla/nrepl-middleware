(ns gorilla-middleware.render_values-test
  (:require [clojure.test :refer :all]
    ;; hack to test just compilation for now
            [gorilla-middleware.handle :refer [nrepl-handler]]
            [gorilla-middleware.json :as json]
            [gorilla-middleware.render-values :refer :all]))

(deftest json-serialization
  (testing "Default JSON serialization"
    (is (= (json/serialize {:a 1}) "{\"a\":1}"))))
