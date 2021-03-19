(ns pinkgorilla.nrepl.client.op.gorilla
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [key-concat key-concat-conj]]))

; status is retured for all 3 req-types

; used by notebook to regularly display status of smiffer
(defmethod init :sniffer-status [req]
  (key-concat [:sniffer-status]))

; register a source (used by client app, it is more clean than :gorilla/on)
(defmethod init :sniffer-source [req]
  (key-concat [:sniffer-status]))

; register a sink (used by notebook to listen to sniffed evals)
;(defmethod init :sniffer-sink [req]
;  (multiple-key-concat [:sniffer-status]))


(defn- process-fragment-sink
  "result contains the accumulated eval-res messages.
   processes a fragment-response and modifies result-atom accordingly."
  [result res]
  (let [{:keys [sniffer-forward sniffer-status]} res]
    (cond
      sniffer-forward (dissoc sniffer-forward :nrepl.middleware.print/keys)
      sniffer-status sniffer-status
      :else :no-response)))

(defmethod init :sniffer-sink [req]
  {:initial-value {}
   :process-fragment process-fragment-sink})

;

(defmethod init :gorilla-nav [req]
  (key-concat-conj [:datafy]))
