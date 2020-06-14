(ns demo.core
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [ring.util.response :as response]
   [nrepl.server]
   [pinkgorilla.middleware.cider :refer [cider-handler]]
   ;[com.stuartsierra.component :as c]
   [ring.adapter.jetty9 :refer [run-jetty]]
   ;[pinkgorilla.nrepl.server :refer [new-cider-repl-server]]
   ;[pinkgorilla.web.serving-with-jetty :refer [add-jetty-server]]
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

(defn run-nrepl-gateway []
  (timbre/set-level! :debug)
  (info "starting..")
  (println "starting nrepl server at port 12000 ..")
  (let [nrepl-server (nrepl.server/start-server :port 12000
                                                :handler (cider-handler))
        ws-handler (ws-processor nrepl-server)
        _ (println "handler: " ws-handler)]
    (run-jetty ws-handler {:port 9000
                           :websockets {"/nrepl" ws-handler}
                           :allow-null-path-info true})))

(defn -main [& args]
  (println "nrepl proxy starting with cli-args: " args)
  (run-nrepl-gateway))


