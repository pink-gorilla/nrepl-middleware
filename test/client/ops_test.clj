(ns client.ops-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.nrepl.client.core :refer [process-req]]))

; :ns needs to be in fragment, because otherwise value and pcasso
; do not get processed.
; picasso is wrapped as edn by middleware, you can run (pr-str val)
; to create more tests

(def code {:req {:op :eval :code "1 (println 4) (+ 1 1) (println 5) 3"}
           :fragments [{:value 1 :ns "user" :picasso "1"}
                       {:out "4"}
                       {:value 2 :ns "user" :picasso "2"}
                       {:out "5"}
                       {:value 3 :ns "user" :picasso "3"}]
           :result {:value [1 2 3]
                    :picasso [1 2 3]
                    :ns "user"
                    :out "45"
                    :err []
                    :root-ex nil
                    }})

(deftest ops-fragment-processing
  (testing "Default EDN serialization"
    (is (= (process-req code) (:result code)))

    ;
    ))
