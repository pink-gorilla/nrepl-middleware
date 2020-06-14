(ns demo.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer [debug info warn error]]
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [cljs-uuid-utils.core :as uuid]
   [pinkgorilla.nrepl.ws.connection :refer [ws-connect!]]))

(enable-console-print!)

(defn ^:export  start []
  (js/console.log "Starting...")
  (println "starting with println")
  #_(reagent.dom/render [demo.app/app]
                        (.getElementById js/document "app"))

  (let [{:keys [session-id ch]} (ws-connect! "ws://localhost:9000/nrepl")]
    (go-loop []
      (let [msg (<! ch)]
        (info "rcvd: " msg)
        (recur)))
    (go-loop []
      (<! (timeout 5000))
      (info "session id: " @session-id)
      (>! ch {:op "describe" :id (uuid/uuid-string (uuid/make-random-uuid))})
      (<! (timeout 15000))
      (recur))))

;(make-request! conn {:op "describe"})

