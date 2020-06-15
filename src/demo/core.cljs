(ns demo.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [reagent.dom]
   [reagent.core :as r]
   [cljs-uuid-utils.core :as uuid]
   [pinkgorilla.nrepl.ws.connection :refer [ws-connect!]]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-client! nrepl-op]]
   [pinkgorilla.nrepl.op.describe :refer [describe-req ls-sessions]]
   [pinkgorilla.nrepl.op.eval :refer [nrepl-eval]]
   [pinkgorilla.nrepl.op.cider :refer [stacktrace resolve-symbol doc-string completions]]
   [demo.ui :refer [app]]))

(enable-console-print!)

;(timbre/set-level! :trace) ; Uncomment for more logging
;  (timbre/set-level! :debug)
(timbre/set-level! :info)

(defn conn-raw 
  "demo nrepl websocket 
   uses the level1 api (raw api)
   only useful for testing"
  [ws-url]
  (let [{:keys [connected? session-id input-ch output-ch] :as conn}
        (ws-connect! ws-url)]

    ; print rcvd messages
    (go-loop []
      (let [msg (<! output-ch)]
        (info "DEMO RCVD: " msg)
        (recur)))

     ; send messages
    (go-loop []
      (<! (timeout 5000))
      (info "connected: " @connected? "session id: " @session-id)
      (>! input-ch {:op "describe" :id (uuid/uuid-string (uuid/make-random-uuid))})
      (<! (timeout 15000))
      (recur))

    conn))

(defn conn-req 
  "demo nrepl websocket
   uses the async request api (layer 2)"
  [ws-url d]
  (let [{:keys [connected? session-id] :as conn}
        (nrepl-client! ws-url)]
 ; send messages
    (go-loop [c 1]
      (<! (timeout 5000))
      (info "connected: " @connected? "session id: " @session-id)
      
      #_(let [c (<! (nrepl-op conn {:op "describe"}))]
        (info "first fragment: " c)
        (reset! d c))
      
      (let [c (<! (describe-req conn))]
        (info "describe result: " c)
        (reset! d c)
        )
      
      (let [r (<! (nrepl-eval conn "(println 3)(* 7 7)(println 5)"))]
        (info "eval result: " r)
        ;(reset! d c)
        )
      
      (let [r (<! (nrepl-eval conn "(throw Exception \"b\")"))
            x (<! (stacktrace conn))
            ]
        (info "stacktrace result: " x)
        ;(reset! d c)
        )
      
        (let [r (<! (resolve-symbol conn "pprint-table" "clojure.pprint"))]
          (info "resolve symbol result: " r)
        ;(reset! d c)
          )

         (let [r (<! (doc-string conn "doseq" "clojure.core"))]
           (info "docstring result: " r)
        ;(reset! d c)
           )
      
      
         (let [r (<! (completions conn "ma" "user" "(def a 4)"))]
           (info "completion result: " r)
        ;(reset! d c)
           )
        
           (let [r (<! (ls-sessions conn))]
             (info "ls-sessions result: " r)
        ;(reset! d c)
             )

        
        
        
      
      
      
      
      (<! (timeout 15000))
      (recur 1))
    conn))

(defn ^:export  start []
  (js/console.log "Starting...")
  (println "starting with println")

  (let [d (r/atom nil)
        ;conn (conn-raw "ws://localhost:9000/nrepl")
        conn (conn-req "ws://localhost:9000/nrepl" d)]

    (reagent.dom/render [app conn d]
                        (.getElementById js/document "app"))))

;(make-request! conn {:op "describe"})

