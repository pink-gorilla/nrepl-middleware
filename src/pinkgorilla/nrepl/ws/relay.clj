(ns pinkgorilla.nrepl.ws.relay
  (:require
   [taoensso.timbre :refer [debug info error]]
   [clojure.edn :as edn]
   [nrepl.server :as nrepl-server]
   [nrepl.core :as nrepl]))

;; Not as nice as doall, but doall does not work with piped transports / read-timeout (in mem)
(defn- process-replies
  [reply-fn contains-pred replies-seq]
  (loop [s replies-seq]
    (let [msg (first s)]
      (reply-fn msg)
      (when-not (contains-pred (:status msg))
        (recur (rest s))))))

(defn pr-str-with-meta [data]
  (binding [*print-meta* true]
    (pr-str data)))

(defn process-nrepl-message [ws-send-fn ws-id msg]
  (println "rcvd nrepl msg: " msg)
  (let [payload (pr-str-with-meta msg)]
    (info "ws Send " payload)
    (ws-send-fn ws-id payload)))

(defn make-nrepl-request
  "Processes websocket messages"
  [nrepl-handler
   transport client
   ws-send-fn ws-id
   msg]
  (let [;data-edn (edn/read-string data)
        ;_ (debug "data edn: " data-edn " meta: " (meta data-edn))
        ;msg (assoc data-edn :as-picasso 1)
        [read write] transport
        reply-fn (partial process-replies
                          (partial process-nrepl-message ws-send-fn ws-id)
                          (fn [s] (contains? s :done)))]
    (reply-fn
     ;; TODO: Not redundant do as clj-kondo claims!
     (do
       (when (:op msg)
         (future (nrepl.server/handle* msg nrepl-handler write)))
       (client)))))

(defn on-ws-receive [nrepl-handler transport client
                     ws-send-fn ws-id
                     message]
  (info "request rcvd: " ws-id " msg: " message)
  (let [data-edn (edn/read-string message)
        _ (debug "data edn: " data-edn " meta: " (meta data-edn))
        msg (assoc data-edn :as-picasso 1)]
    (make-nrepl-request nrepl-handler
                        transport client
                        ws-send-fn ws-id
                        msg)))