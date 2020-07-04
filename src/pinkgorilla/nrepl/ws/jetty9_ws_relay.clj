(ns pinkgorilla.nrepl.ws.jetty9-ws-relay
  "A websocket handler that passes messages back and forth to an already running nREPL server."
  (:require
   [taoensso.timbre :refer [debug info error]]
   [clojure.edn :as edn]
   [ring.adapter.jetty9 :as jetty]
   [nrepl.server :as nrepl-server]
   [nrepl.core :as nrepl]
   [nrepl.transport :as transport]
  ; Pinkgorilla Libraries
   [pinkgorilla.nrepl.ws.relay :refer [on-ws-receive]]
   [pinkgorilla.nrepl.middleware.render-values] ; side-effects
   ))

(defn ws-processor
  "Creates a websocket thing (not an actual ring-handler).
   Messages are mapped back and forth to EDN.
   Uses https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/websocket/api/WebSocketAdapter.html"
  [nrepl-handler]
  {:on-connect (fn [_] ;; ws
                 (info "ws Connect"))
   :on-error   (fn [_ e]
                 (error "ws Error" e))
   :on-close   (fn [_ws status-code reason]
                 (info "ws Close" status-code reason))
   :on-text    (fn [ws text-message]
                 (debug "ws Rcvd Text" " " text-message)
                 (let [session (:session ws)
                       _ (debug "session: " session)
                       _ (debug "ws: " ws)
                       transport (or (::transport session)
                                     (transport/piped-transports))]
                   (on-ws-receive @nrepl-handler transport
                                  jetty/send! ws
                                  text-message)))
   :on-bytes   (fn [_ _ _ _] ;; ws bytes offset len
                 (info "ws Bytes"))})
