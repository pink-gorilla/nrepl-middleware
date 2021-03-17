(ns client.xf-join-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.nrepl.client.transducer :refer [req-res-join res-transform-conj res-transform-extract]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

; sniffr messages are created with:
; lein client -m sink
(def sniffer-messages
  [{:code "\"YES\"", :id "6e465883-adc1-458c-8200-7fed7d581236", :op "eval"}
   {:datafy "{:idx 30, :value \"YES\", :meta nil}", :id "6e465883-adc1-458c-8200-7fed7d581236", :meta "nil", :ns "user", :picasso "{:type :hiccup, :content [:span {:class \"clj-string\"} \"\\\"YES\\\"\"]}", :value "YES"}
   {:id "6e465883-adc1-458c-8200-7fed7d581236", :status ["done"]}

   {:as-picasso 1, :code "^:R [:p (+ 8 8)]", :id "eab5bcb1-a9d5-4ad3-ad66-d871eaabff9d", :op "eval"}
   {:datafy "{:idx 32, :value [:p 16], :meta {:R true}}", :id "eab5bcb1-a9d5-4ad3-ad66-d871eaabff9d", :meta "{:R true}", :ns "user", :picasso "{:type :reagent, :content {:hiccup ^{:R true} [:p 16], :map-keywords true}}", :value ["p" 16]}
   {:id "eab5bcb1-a9d5-4ad3-ad66-d871eaabff9d", :status ["done"]}

   {:code "(+ 1 1) (+ 2 2) (+ 3 3)", :id "1a193a83-cf0e-4ca7-ba26-5c9943f3126d", :op "eval"}
   {:datafy "{:idx 33, :value 2, :meta nil}", :id "1a193a83-cf0e-4ca7-ba26-5c9943f3126d", :meta "nil", :ns "user", :picasso "{:type :hiccup, :content [:span {:class \"clj-long\"} \"2\"]}", :value 2}
   {:datafy "{:idx 34, :value 4, :meta nil}", :id "1a193a83-cf0e-4ca7-ba26-5c9943f3126d", :meta "nil", :ns "user", :picasso "{:type :hiccup, :content [:span {:class \"clj-long\"} \"4\"]}", :value 4}
   {:datafy "{:idx 35, :value 6, :meta nil}", :id "1a193a83-cf0e-4ca7-ba26-5c9943f3126d", :meta "nil", :ns "user", :picasso "{:type :hiccup, :content [:span {:class \"clj-long\"} \"6\"]}", :value 6}
   {:id "1a193a83-cf0e-4ca7-ba26-5c9943f3126d", :status ["done"]}])

#_(let [rr (into []
                 (req-res-join res-transform-extract false)
                 sniffer-messages)]
    (for [r rr]
      (println r)))

(deftest xf-join-extract-no-partial
  (let [req-res (into [] (req-res-join res-transform-extract false) sniffer-messages)
        v (fn [n]
            (-> (nth req-res n)
                (get-in [:value])))]
    ;(println "0: " (nth req-res 0))
    (testing "transducer for req-res-join"
      (is (= 3 (count req-res)))
      (is (= ["YES"] (v 0)))
      (is (= [["p" 16]] (v 1)))
      (is (= [2 4 6] (v 2)))
     ;   
      )))
