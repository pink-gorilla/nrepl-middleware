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
   :result [{:x 1} {:y 2}]})

(deftest ops-fragment-processing
  (testing "ops-fragment-processing"
    (is (= (process-req code) (:result code)))
    (is (= (process-req datafy) (:result datafy)))

    ;
    ))
