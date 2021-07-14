(ns pinkgorilla.nrepl.client.connection
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn warnf errorf]]

   [pinkgorilla.nrepl.client.connection.bencode :as bencode]
   [pinkgorilla.nrepl.client.connection.in-process :as in-process]))

(defn connect! [{:keys [type] :as config}]
  (case type
    :bencode (bencode/connect! config)
    :in-process (in-process/connect config)
    (bencode/connect! config)))

(defn disconnect! [{:keys [config] :as s}]
  (let [type (:type config)]
    (info "disconnecting nrepl conn type: " type)
    (case type
      :bencode (bencode/disconnect! s)
      :in-process (in-process/disconnect! s)
      (bencode/disconnect! s))))

