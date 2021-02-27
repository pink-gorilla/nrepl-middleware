(ns pinkgorilla.nrepl.client.op.concat
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn error]]
      :clj [taoensso.timbre :refer [debug info warn error]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

; many :op initsjust return one value. 
; more efficient by having this helper
(defn single-key-concat [k]
  {:initial-value [] ;{k []}
   :process-fragment
   (fn [result fragment]
     (info "prior result: " result)
     (conj result (get fragment k)))})

(defn multiple-key-concat [ks]
  {:initial-value [] ;{k []}
   :process-fragment
   (fn [result fragment]
     (info "prior result: " result)
     (conj result (select-keys fragment ks)))})

(defmethod init :gorilla-nav [req]
  (single-key-concat :datafy))

