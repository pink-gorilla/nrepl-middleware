(ns pinkgorilla.nrepl.op.admin
  (:require
   [clojure.string :as str]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))


;todo:
; op: close
; op: clone

(defn describe [conn]
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
       (apply conj (map :sessions fragments))
     )))


