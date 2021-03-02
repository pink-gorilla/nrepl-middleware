(ns pinkgorilla.nrepl.client.op.gorilla
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [ multiple-key-concat]]))

; status is retured for all 3 req-types

; used by notebook to regularly display status of smiffer
(defmethod init :sniffer-status [req] 
  (multiple-key-concat [:sniffer-status]))

; register a source (used by client app, it is more clean than :gorilla/on)
(defmethod init :sniffer-source [req]
  (multiple-key-concat [:sniffer-status]))

; register a sink (used by notebook to listen to sniffed evals)
;(defmethod init :sniffer-sink [req]
;  (multiple-key-concat [:sniffer-status]))


(defn- process-fragment
  "result is an atom, containing the eval result.
   processes a fragment-response and modifies result-atom accordingly."
  [result res]
  {:res (:sniffer-forward res)
   :count (inc (:count result))})


(defmethod init :sniffer-sink [req]
  {:initial-value {:count 0}
   :process-fragment process-fragment})

; 
