(ns pinkgorilla.nrepl.client.op.gorilla
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [single-key-concat multiple-key-concat]]))



(defmethod init :sniffer-status [req]
  (multiple-key-concat [:sniffer-status]))

(defmethod init :sniffer-source [req]
  (multiple-key-concat [:sniffer-status]))


; :sniffer-forward
