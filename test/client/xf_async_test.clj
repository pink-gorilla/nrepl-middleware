(ns client.xf-async-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [clojure.core.async :refer [chan onto-chan to-chan! close! go  go-loop >! <! <!!]]
   [pinkgorilla.nrepl.client.transducer :refer [res-transform-conj res-transform-extract]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   [client.xf-transform-test :refer [ress]]))

(defn parse []
  (let [res (atom [])
        c (chan 1 (res-transform-extract false))]
    ; put onto chan
    (go
      (onto-chan c ress)) ; ress is the unit test input data
    ; read all available results from chan
    (loop [r (<!! c)]
      (when r ; nil when chan is closed
        ;(println "r:" n) 
        (reset! res r)
        (recur (<!! c))))
      ;
    @res))

(deftest xf-async-parse
  (testing "transducer async parse"
    (is (= [2 4 6] (:value (parse))))
    ;
    ))

(comment
  (parse)
  ;
  )

