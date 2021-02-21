(ns pinkgorilla.nrepl.service.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   ;[ring.util.response :as response]
   ;[ring.middleware.cors :refer [wrap-cors]]
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

(defn run-relay-jetty [config]
  (require 'ring.adapter.jetty9)
  (let [ws-handler (jetty-relay-handler)
        run-jetty (resolve 'ring.adapter.jetty9/run-jetty)
        {:keys [port route]} (:relay config)]
    (info "starting jetty relay at port " port "..")
    (run-jetty ws-handler {:port port
                           :websockets {route ws-handler}
                           :allow-null-path-info true
                           ;:join?  false        
                           })))




