(ns demo.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer [debug info warn error]]
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [reagent.dom]
   [cljs-uuid-utils.core :as uuid]
   [pinkgorilla.nrepl.ws.connection :refer [ws-connect!]]))

(enable-console-print!)

(defn nrepl-conn-info [conn]
  (let [{:keys [connected? session-id]} conn]
    [:div 
     [:p "NRepl connected:" (str @connected?)]
      [:p "NRepl ession-id:" (str @session-id)]
    ]
    )
  )

(defn app [conn]
  [:div
   [:h1 "NRepl demo"]
   [nrepl-conn-info conn]
   ]
  )

(defn ^:export  start []
  (js/console.log "Starting...")
  (println "starting with println")

  (let [{:keys [connected? session-id input-ch output-ch] :as conn} (ws-connect! "ws://localhost:9000/nrepl")]
    (go-loop []
      (let [msg (<! output-ch)]
        (info "DEMO RCVD: " msg)
        (recur)))
    (go-loop []
      (<! (timeout 5000))
      (info "connected: " @connected? "session id: " @session-id)
      (>! input-ch {:op "describe" :id (uuid/uuid-string (uuid/make-random-uuid))})
      (<! (timeout 15000))
      (recur))
    
      (reagent.dom/render [app conn]
                            (.getElementById js/document "app"))
    
    ))

;(make-request! conn {:op "describe"})

