(ns client.xf-async-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [clojure.core.async :refer [chan to-chan! close! go  go-loop >! <! <!!]]
   [pinkgorilla.nrepl.client.transducer :refer [req-res-join res-transform-conj res-transform-extract]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

#_(defn req-eval [req res-v]
    (let [xf (res-for-req-eval req)]
      (into [] xf res-v)))

#_(deftest xf-eval
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

#_(defn run []
    (let [res (atom [])
          in-ch (to-chan! [{:id 1 :out "1" :ns "user" :value 7}
                           {:id 1 :out "2"}
                           {:id 1 :out "3"}
                           {:id 1 :out "4" :ns "yuppi" :value 9}
                           {:id 1 :out "5"}])
          out-ch (chan)
          xf (res-for-req-eval {:op :eval :id 1})]
   ; read from in-chan and put on out chan
      (go-loop [v (<! in-ch)]
        (if-not v
          (close! out-ch)
          (do
            (>! out-ch (xf v))
            (recur (<! in-ch)))))
   ; read from out-chan and add to atom
      (let [r (<!! (go-loop [v (<! out-ch)]
                     (println "out:" v)
                     (if-not v
                       (do (close! out-ch)
                           @v)
                       (do
                         (swap! res (conj v))
                         (recur (<! out-ch))))))]

        r)))

;(run)