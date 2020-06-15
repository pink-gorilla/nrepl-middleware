(ns pinkgorilla.nrepl.op.describe
  (:require
   [clojure.string :as str]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))


(defn describe-req [conn]
  (nrepl-op-complete
   conn
   {:op "describe"}
   (fn [fragments]
     (let [f (first fragments)]
          (select-keys f [:versions :ops])))))


(defn ls-sessions [conn]
  (nrepl-op-complete
   conn
   {:op "ls-sessions"}
   (fn [fragments]
       (reduce #(conj  (:sessions %2) %1 ) () fragments
               ;(map :sessions fragments)
               )
     
     )))


