(ns pinkgorilla.nrepl.client.op.concat
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug debugf info warn warnf error]]
      :clj [taoensso.timbre :refer [debug debugf info warn warnf error]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

; many :op responses are just one message with one or more keys. 

(defn key-concat [ks]
  {:initial-value {}
   :process-fragment
   (fn [result fragment]
     (let [data (select-keys fragment ks)]
       (debugf "prior result: %s new: %s" result data)
       (merge result data)))})

(defn key-concat-conj [ks]
  {:initial-value []
   :process-fragment
   (fn [result fragment]
     (let [data (select-keys fragment ks)]
       (debugf "prior result: %s new: %s" result data)
       (conj result data)))})

; unknown op responses will get returned by conj-ing all responses

(defn develop-concat []
  {:initial-value []
   :process-fragment
   (fn [result fragment]
     (debug "prior result: " result)
     (conj result (dissoc fragment :session :transport :id)))})

(defmethod init :default [req]
  (warnf "using default op processor for op: %s" (:op req))
  (develop-concat))
