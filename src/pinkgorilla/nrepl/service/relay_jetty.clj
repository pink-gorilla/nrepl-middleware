(ns pinkgorilla.nrepl.service.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   ;[ring.util.response :as response]
   ;[ring.middleware.cors :refer [wrap-cors]]
   [ring.adapter.jetty9 :refer [run-jetty]]
   [ring.middleware.session.memory :as memory]  ; contained in ring
   [ring.middleware.session :as session] ; contained in ring
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]
   [pinkgorilla.nrepl.ws.jetty9-ws-relay :refer [ws-processor]]))

(defn jetty-relay-handler []
  (let [nrepl-handler (atom (make-default-handler))
        ws-handler (ws-processor nrepl-handler)
        _ (info "relay ws-handler: " ws-handler)
        ;ws-handler-wrapped (-> ws-handler (wrap-cors :access-control-allow-origin #".+"))
        ]
  ;ws-handler-wrapped
    ws-handler))

(defn wrap-memory-session
  "Wraps the supplied handler in session middleware that uses a
    private memory store."
  [handler]
  (let [store (memory/memory-store)]
    (session/wrap-session handler
                          {:store store
                           :cookie-name "nrepl-relay-session"})))

(defn run-relay-jetty [config]
  (let [ws-handler (jetty-relay-handler)
        ws-handler-wrapped (wrap-memory-session ws-handler)
        {:keys [port route]} (:relay config)]
    (info "starting jetty relay at port " port "..")
    (run-jetty ws-handler-wrapped
               {:port port
                :websockets {route ws-handler} ; wrapped does not work
                :allow-null-path-info true
                ;:join?  false        
                })))




