(ns demo.app
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   
   [ring.util.response :as response]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.adapter.jetty9 :refer [run-jetty]]
   [nrepl.server]
   [picasso.default-config] ; for side effects   
   [pinkgorilla.nrepl.middleware.cider :refer [cider-handler]]
   [pinkgorilla.nrepl.ws.jetty9-ws-relay :refer [ws-processor]])
  (:gen-class))

(defn html-response [html]
  (response/content-type
   {:status 200
    :body html}
   "text/html"))

(defn dummy-handler [req]
  (html-response "Hello, World!")
  nil)

(def nrepl-handler
  "this is only needed by jetty-ws-relay and jee-interop
   httpkit handler has it in its own namespace"
  (atom (cider-handler)))

(defn run-nrepl-relay []
  (timbre/set-level! :debug)
  (info "starting nrepl server at port 12000 ..")
  (let [nrepl-server (nrepl.server/start-server :port 12000
                                                :handler (cider-handler))
        ws-handler (ws-processor nrepl-handler)
        _ (println "handler: " ws-handler)
        ws-handler-wrapped ws-handler
        ;ws-handler-wrapped (-> ws-handler (wrap-cors :access-control-allow-origin #".+"))
        ]
    (run-jetty ws-handler-wrapped {:port 9000
                           :websockets {"/nrepl" ws-handler-wrapped}
                           :allow-null-path-info true
                           ;:join?  false        
                                   })))

(defn -main [& args]
  (println "nrepl relay starting with cli-args: " args)
  (run-nrepl-relay))


