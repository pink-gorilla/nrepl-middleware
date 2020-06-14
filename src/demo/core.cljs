(ns demo.core
  (:require
   ; demo
   [pinkgorilla.nrepl.ws.client :refer [ws-start! make-request!]]))

(enable-console-print!)

(defn ^:export  start []
  (js/console.log "Starting...")
  (println "starting with println")
  #_(reagent.dom/render [demo.app/app]
                        (.getElementById js/document "app"))

  (let [conn (ws-start! "ws://localhost:9000/nrepl")]
    (make-request! conn {:op "describe"})))

