(ns pinkgorilla.nrepl.service.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   ;[ring.util.response :as response]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.adapter.jetty9 :refer [run-jetty]]
   [pinkgorilla.nrepl.middleware.cider :refer [cider-handler]]
   [pinkgorilla.nrepl.ws.jetty9-ws-relay :refer [ws-processor]]
   [pinkgorilla.nrepl.client] ; side-effects
   ))

(defn jetty-relay-handler []
  (let [nrepl-handler (atom (cider-handler))
        ws-handler (ws-processor nrepl-handler)
        _ (info "relay ws-handler: " ws-handler)
        ;ws-handler-wrapped (-> ws-handler (wrap-cors :access-control-allow-origin #".+"))
        ]
  ;ws-handler-wrapped
    ws-handler))

(defn run-relay-jetty [config]
  (let [ws-handler (jetty-relay-handler)
        relay-config (:relay config)
        {:keys [port route]} relay-config]
    (info "starting jetty relay at port " port "..")
    (run-jetty ws-handler {:port port
                           :websockets {route ws-handler}
                           :allow-null-path-info true
                           ;:join?  false        
                           })))




