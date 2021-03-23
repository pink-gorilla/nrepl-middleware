(ns pinkgorilla.nrepl.client.op.gorilla
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [tracef debug debugf info infof warn warnf error errorf]]
      :clj [taoensso.timbre         :refer [tracef debug debugf info infof warn warnf error errorf]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [key-concat key-concat-conj]]))

; status is retured for all 3 req-types

; used by notebook to regularly display status of sniffer
(defmethod init :sniffer-status [req]
  (key-concat [:sniffer-status]))

; register a source (used by client app, it is more clean than :gorilla/on)
(defmethod init :sniffer-source [req]
  (key-concat [:sniffer-status]))

; register a sink (used by notebook to listen to sniffed evals)

(defn- process-fragment-sink
  "result contains the accumulated eval-res messages.
   processes a fragment-response and modifies result-atom accordingly."
  [result res]
  (let [{:keys [sniffer-forward sniffer-status]} res]
    (cond
      sniffer-forward
      (let [sniffer-forward (dissoc sniffer-forward :nrepl.middleware.print/keys)]
        (infof "sniffer-sink-forward: %s" sniffer-forward)
        sniffer-forward)

      sniffer-status
      sniffer-status

      :else
      :no-response)))

(defmethod init :sniffer-sink [req]
  {:initial-value {}
   :process-fragment process-fragment-sink})

;

(defmethod init :gorilla-nav [req]
  (key-concat-conj [:datafy]))
