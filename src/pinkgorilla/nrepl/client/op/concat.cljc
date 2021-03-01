(ns pinkgorilla.nrepl.client.op.concat
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn warnf error]]
      :clj [taoensso.timbre :refer [debug info warn warnf error]])
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
     (debug "prior result: " result "ks: " ks)
     (conj result (select-keys fragment ks)))})

(defmethod init :gorilla-nav [req]
  (single-key-concat :datafy))

(defn develop-concat []
  {:initial-value [] ;{k []}
   :process-fragment
   (fn [result fragment]
     (debug "prior result: " result)
     (conj result (dissoc fragment :session :transport :id)))})

(defmethod init :default [req]
  (warnf "using default op processor for op: %s" (:op req))
  (develop-concat))
