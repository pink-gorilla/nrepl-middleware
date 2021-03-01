(ns client.transducer-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.nrepl.client.transducer :refer [xf-res-for-req-eval]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

(defn req-eval [req res-v]
  (let [xf (xf-res-for-req-eval req)]
    (into [] xf res-v)))

(deftest xf-eval
  (testing "transducer for :eval"
    (is (= (req-eval {:op :eval :id 1}
                     [{:id 1 :out "1" :ns "user" :value 7}
                      {:id 1 :out "2"}
                      {:id 1 :out "3"}
                      {:id 1 :out "4" :ns "yuppi" :value 9}
                      {:id 1 :out "5"}])
           [{:value [7 9]
             :picasso [nil nil]
             :ns "yuppi"
             :out "12345"
             :err []
             :root-ex nil}]))
    ;
    ))

