(ns client.xf-transform-test
  (:require
   [clojure.test :refer [testing is deftest]]
   [pinkgorilla.nrepl.client.transducer :refer [res-transform-conj res-transform-extract]]
   [pinkgorilla.nrepl.client.core] ; side-effects
   ))

(def ress
  [{:req {:op "eval" :code "(+ 1 1) (+ 2 2) (+ 3 3)"}}
   {:res {:ns "user" :value 2} :req {:op "eval" :code "(+ 1 1) (+ 2 2) (+ 3 3)"}}
   {:res {:ns "user" :value 4} :req {:op "eval" :code "(+ 1 1) (+ 2 2) (+ 3 3)"}}
   {:res {:ns "user" :value 6} :req {:op "eval" :code "(+ 1 1) (+ 2 2) (+ 3 3)"}}
   {:res {:status ["done"]}    :req {:op "eval" :code "(+ 1 1) (+ 2 2) (+ 3 3)"}}])

(deftest xf-join
  (testing "transform-conj partials"
    (let [req-res (into [] (res-transform-conj true) ress)
          v (fn [n]
              (as-> n x
                (nth req-res x)
                (get-in x [:res])
                (map :value x)
                (into [] x)
                   ;first
                ))]
      (is (= (count req-res) 5))
      (is (= [] (v 0)))
      (is (= [2] (v 1)))
      (is (= [2 4] (v 2)))
      (is (= [2 4 6] (v 3)))
      (is (= [2 4 6 nil] (v 4)))
    ;
      )))

(deftest xf-join-no-partials
  (testing "transform-conj no-partials"
    (let [req-res (into [] (res-transform-conj false) ress)
          v (fn [n]
              (as-> n x
                (nth req-res x)
                (get-in x [:res])
                (map :value x)
                (into [] x)
                   ;first
                ))]
      (is (= (count req-res) 1))
      (is (= [2 4 6 nil] (v 0)))
    ;
      )))

(deftest xf-extract
  (testing "transformation res-extract"
    (let [req-res (into [] (res-transform-extract true) ress)
          v (fn [n]
              (-> (get req-res n)
                  (get-in [:res :value])))]
      (is (= (count req-res) 5))
      (is (= (v 0) []))
    ;
      )))