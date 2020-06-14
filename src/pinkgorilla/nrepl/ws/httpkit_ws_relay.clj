(ns pinkgorilla.nrepl.ws.httpkit-ws-relay
  "A websocket handler that passes messages back and forth to an already running nREPL server."
  ;(:use [compojure.core :only (defroutes GET)]
  ;      ring.util.response
  ;      ring.middleware.cors)
  (:require
   [taoensso.timbre :refer [debug info error]]
   [clojure.edn :as edn]
   [org.httpkit.server :as http]
   [nrepl.server]
   [nrepl.core]
   [nrepl.transport]
   [pinkgorilla.middleware.cider :refer [cider-handler]]))

(def clients (atom {}))
(def my-cider-handler (cider-handler))

(defn- process-replies
  [reply-fn contains-pred replies-seq]
  (loop [s replies-seq]
    (let [msg (first s)]
      (reply-fn msg)
      (if-not (contains-pred (:status msg))
        (recur (rest s))))))

(defn pr-str-with-meta [data]
  (binding [*print-meta* true]
    (pr-str data)))

(defn process-nrepl-message [con-id msg]
  (println "rcvd nrepl msg: " msg)
  (let [payload (pr-str-with-meta msg)]
    (info "ws Send " payload)
    (http/send! con-id payload)))

(defn make-nrepl-request
  "Processes websocket messages"
  [transport con-id msg]
  (let [timeout Long/MAX_VALUE
        [read write] transport
        client (nrepl.core/client read timeout)
        reply-fn (partial process-replies
                          (partial process-nrepl-message con-id)
                          (fn [s] (contains? s :done)))]
    (reply-fn
     ;; TODO: Not redundant do as clj-kondo claims!
     (do
       (when (:op msg)
         (future (nrepl.server/handle* msg my-cider-handler write)))
       (client)))))


(defn process-ws-request [con-id transport message]
  (println "request rcvd: " con-id " msg: " message)
  (let [data-edn (edn/read-string message)
        _ (debug "data edn: " data-edn " meta: " (meta data-edn))
        msg (assoc data-edn :as-html 1)]
    (make-nrepl-request transport con-id msg)))

(defn ws-handler
  [req]
  (http/with-channel req con
    (println "nrepl relay: ws-client connected! conn: " con)
    (let [transport (nrepl.transport/piped-transports)]
      (swap! clients assoc con transport)
      (http/on-receive con (partial process-ws-request con transport))
      (http/on-close con (fn [status]
                           (swap! clients dissoc con)
                           (println "nrepl relay: ws-client disconnected. status: " status "con:" con))))))


;; ASYNC IO based websocket handler:
;(defn handler
;  [request]
;  (let [c (process request)] ;; long running process that returns a channel
;    (http/with-channel request channel
;      (http/send! channel {:status 200
;                           :body (<!! (go (<! c)))))
;      (http/on-close channel (fn [_] (async/close! c))))))


; send heartbeats to all connected websockets.
#_(future
  (loop []
    (doseq [client @clients]
      (http/send! (key client)
                  (pr-str {:heartbeat (rand 10)})
                  false))
    (Thread/sleep 10000)
    (recur)))
