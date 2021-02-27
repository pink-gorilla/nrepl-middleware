(ns pinkgorilla.nrepl.relay.jetty9-ws-relay
  "A websocket handler that passes messages back and forth to an already running nREPL server."
  (:require
   [taoensso.timbre :refer [debug info error]]
   [ring.adapter.jetty9 :as jetty]
   [nrepl.transport :as transport]
   [nrepl.core :as nrepl]
   [pinkgorilla.nrepl.relay.relay :refer [on-ws-receive]]))

(defn connect [transport]
  (let [timeout Long/MAX_VALUE
        [read write] transport
        client (nrepl.core/client read timeout)]
    client))

(def clients (atom {}))

(defn client-get [ws-id]
  (get @clients ws-id))

(defn client-close [ws-id]
  (when-let [c (client-get ws-id)]
    (when-let [client (:client c)]
      (info "client closed: " ws-id)
      (swap! clients dissoc ws-id)
      (client {:op "close"}))))

(defn client-save [ws-id transport client]
  (let [c {:client client
           :transport transport}]
    (info "saving client " ws-id)
    (swap! clients assoc ws-id c))

  (when (> (count @clients) 1)
    (info "more than one client: " (count @clients))
    (doall (for [t (keys @clients)]
             (do ;(info "client:" t)
               (if (identical? ws-id t)
                 nil
                 (client-close t)))))))

(defn ws-processor
  "Creates a websocket thing (not an actual ring-handler).
   Messages are mapped back and forth to EDN.
   Uses https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/websocket/api/WebSocketAdapter.html"
  [nrepl-handler]
  {:on-connect (fn [ws]
                 (info "ws Connect")
                 (let [session (:session ws)
                       _ (debug "session: " session)
                       _ (debug "ws: " ws)
                       transport (or (::transport session)
                                     (transport/piped-transports))
                       client (connect transport)]
                   (client-save ws transport client)))
   :on-error   (fn [_ e]
                 (error "ws Error" e))
   :on-close   (fn [_ ws status-code reason]
                 (info "ws Close" status-code reason ws)
                 ;(-close ws)
                 )
   :on-text    (fn [ws text-message]
                 (debug "ws Rcvd Text" " " text-message)
                 (let [c (client-get ws)
                       transport (:transport c)
                       client (:client c)]
                   (->> (on-ws-receive @nrepl-handler transport client
                                       jetty/send! ws
                                       text-message))))
   :on-bytes   (fn [_ _ _ _] ;; ws bytes offset len
                 (info "ws Bytes"))})
