(ns pinkgorilla.nrepl.client.connection
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn warnf errorf]]
   ;[pinkgorilla.nrepl.client.connection.chord :as chord]
   [pinkgorilla.nrepl.client.connection.sente :as sente]))

(defn connect! [{:keys [type] :as config}]
  (case type
    ;:chord (chord/connect! config)
    :sente (sente/connect! config)
    (sente/connect! config)))

(defn disconnect! [{:keys [config] :as s}]
  (let [type (:type config)]
    (info "disconnecting nrepl conn type: " type)
    (case type
      ;:chord (chord/disconnect! s)
      :sente (sente/disconnect! s)
      (sente/disconnect! s))))

