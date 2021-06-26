(ns client.ops-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

(defn- process-fragment-log [process-fragment result fragment]
  (let [r (process-fragment result fragment)]
    (println "processing: " fragment "result: " r)
    r))

(defn process-req [{:keys [req fragments]}]
  (let [{:keys [initial-value process-fragment]} (init req)
        p (partial process-fragment-log process-fragment)]
    (reduce p initial-value fragments)))

; :ns needs to be in fragment, because otherwise value and pcasso
; do not get processed.
; picasso is wrapped as edn by middleware, you can run (pr-str val)
; to create more tests

(def code
  {:req {:op :eval :code "1 (println 4) (+ 1 1) (println 5) 3"}
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
            :root-ex nil}})

(def datafy
  {:req {:op :gorilla-nav
         :idx 1 :key :a :v :b}
   :fragments [{:datafy {:x 1}}
               {:datafy {:y 2}}]
   :result [{:datafy {:x 1}} {:datafy {:y 2}}]})

(deftest ops-fragment-processing
  (testing "ops-fragment-processing"
    (is (= (:result code)   (process-req code)))
    (is (= (:result datafy) (process-req datafy)))
    ;
    ))

(def sniffer-req
  {:req {:op :sniffer-sink
         :id "21f7fdb3-2d97-4533-8e2e-b3d3d88443ab"}})

(def sniffer-res-1
  {:id "21f7fdb3-2d97-4533-8e2e-b3d3d88443ab"
   :session "2a346b60-ee76-4640-9121-f0c199e5f6ed"
   :sniffer-forward {:op "eval"
                     :code "(+ 2 2)"
                     :id "0a40d786-37c6-45b4-8662-68349487f26b"}})

(def sniffer-res-2
  {:id "21f7fdb3-2d97-4533-8e2e-b3d3d88443ab"
   :session "2a346b60-ee76-4640-9121-f0c199e5f6ed"
   :sniffer-forward {:datafy "{:idx 3, :value 4, :meta nil}"
                     :id "0a40d786-37c6-45b4-8662-68349487f26b"
                     :meta "nil"
                     :ns "user"
                     :picasso "{:type :hiccup, :content [:span {:class \"clj-long\"} \"4\"]}"
                     :value 4}})

(def sniffer-res-3
  {:id "21f7fdb3-2d97-4533-8e2e-b3d3d88443ab"
   :session "2a346b60-ee76-4640-9121-f0c199e5f6ed"
   :sniffer-forward {:id "0a40d786-37c6-45b4-8662-68349487f26b"
                     :status ["done"]}})

(deftest ops-fragment-processing-sniffer-forward
  (let [sniffer-1 (assoc sniffer-req :fragments [sniffer-res-1])
        sniffer-2 (assoc sniffer-req :fragments [sniffer-res-1 sniffer-res-2])
        sniffer-3 (assoc sniffer-req :fragments [sniffer-res-1 sniffer-res-2 sniffer-res-3])]

    (testing "ops-fragment-processing sniffer-forward"
      (is (= {:id "0a40d786-37c6-45b4-8662-68349487f26b"
              :op "eval"
              :code "(+ 2 2)"}
             (process-req sniffer-1)))
      (is (= {:id "0a40d786-37c6-45b4-8662-68349487f26b"
              :datafy "{:idx 3, :value 4, :meta nil}"
              :meta "nil" :ns "user"
              :picasso "{:type :hiccup, :content [:span {:class \"clj-long\"} \"4\"]}"
              :value 4}
             (process-req sniffer-2)))
      (is (= {:id "0a40d786-37c6-45b4-8662-68349487f26b"
              :status ["done"]}
             (process-req sniffer-3)))
    ;
      )))