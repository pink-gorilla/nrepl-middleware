(ns client.transducer-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [clojure.core.async :refer [chan to-chan! close! go  go-loop >! <! <!!]]
   [pinkgorilla.nrepl.client.transducer :refer [xf-res-for-req-eval]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

(defn req-eval [req res-v]
  (let [xf (xf-res-for-req-eval req)]
    (into [] xf res-v)))

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

(defn run []
  (let [res (atom [])
        in-ch (to-chan! [{:id 1 :out "1" :ns "user" :value 7}
                         {:id 1 :out "2"}
                         {:id 1 :out "3"}
                         {:id 1 :out "4" :ns "yuppi" :value 9}
                         {:id 1 :out "5"}])
        out-ch (chan)
        xf (xf-res-for-req-eval {:op :eval :id 1})]
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

(run)