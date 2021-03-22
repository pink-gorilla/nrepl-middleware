(ns pinkgorilla.nrepl.relay.jetty
  (:require
   [taoensso.timbre :as timbre :refer [debug debugf info infof error]]
   ;[ring.util.response :as response]
   ;[ring.middleware.cors :refer [wrap-cors]]
   [ring.adapter.jetty9 :as jetty :refer [run-jetty]]
   [clojure.edn :as edn]
   [ring.middleware.session.memory :as memory]  ; contained in ring
   [ring.middleware.session :as session] ; contained in ring
   [pinkgorilla.nrepl.relay.session :refer [create-session! request! disconnect!]]
   ;[pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]
   ))

(def gconn (atom nil))

(defn parse-req-text [req-text]
  (let [req-edn (edn/read-string req-text)]
    ;(debug "data edn: " req-edn " meta: " (meta req-edn))
    (assoc req-edn :as-picasso 1)))

(defn ws-handler
  "Creates a websocket thing (not an actual ring-handler).
   Messages are mapped back and forth to EDN.
   Uses https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/websocket/api/WebSocketAdapter.html"
  [config]
  {:on-connect (fn [ws]
                 (infof "ws connected: %s connecting to nrepl %s" ws config)
                 (let [ws-send! (fn [res]
                                  (jetty/send! ws (pr-str res)))
                       conn (create-session! config ws-send!)]
                   (reset! gconn conn)))
   :on-error   (fn [_ e]
                 (error "ws Error" e))
   :on-close   (fn [_ ws status-code reason]
                 (info "ws Close" status-code reason ws)
                 (let [conn @gconn]
                   (disconnect! conn))
                 ;(-close ws)
                 )
   :on-text    (fn [ws req-text]
                 (let [conn @gconn
                       req (parse-req-text req-text)]
                   (debugf "req ws %s" req)
                   (request! conn req)))
   :on-bytes   (fn [_ _ _ _] ;; ws bytes offset len
                 (info "ws Bytes"))})

(defn wrap-memory-session
  "Wraps the supplied handler in session middleware that uses a
    private memory store."
  [handler]
  (let [store (memory/memory-store)]
    (session/wrap-session handler
                          {:store store
                           :cookie-name "nrepl-relay-session"})))

(defn run-relay-jetty [config]
  (let [ws-handler (ws-handler (:nrepl-client config))
        ws-handler-wrapped (wrap-memory-session ws-handler)
        {:keys [port route]} (:relay config)]
    (info "starting jetty relay at port " port "..")
    (run-jetty ws-handler-wrapped
               {:port port
                :websockets {route ws-handler} ; wrapped does not work
                :allow-null-path-info true
                ;:join?  false        
                })))