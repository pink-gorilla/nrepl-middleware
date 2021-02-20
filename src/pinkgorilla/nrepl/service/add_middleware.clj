(ns pinkgorilla.nrepl.service.add-middleware
  (:require
   [clojure.core.async :refer [<! <!! go]]
   [pinkgorilla.nrepl.client :as client]
   [pinkgorilla.nrepl.helper :refer [print-eval-result status success?]]
   [pinkgorilla.nrepl.handler.nrepl-loader :refer [ops-sniffer]]))

(def state-a (atom nil))

(defn current-session-id []
  (when-let [state @state-a]
    (when-let [session-id (:session-id @state)]
      session-id)))



(defn status-e [fragments]
  (let [s (status fragments)
        e (filter #(= "error" %) s)]
    (if (= 0 (count e))
      (str "Success!")
      (str "Error: " s))))

(defn send! [msg]
  (if-let [send-fn (:send-fn @state-a)]
    (do
      ;(println "sniffer state: " @state-a)
      (send-fn msg))
    (println "send failed - not started")))

 ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  ; (if-let [new-session (:new-session message)]
  ; "interrupt", which will attempt to interrupt the current execution with id provided in the :interrupt-id slot.

; "close", which drops the session indicated by the ID in the :session slot. 
; The response message's :status will include :session-closed.




(defn add-middleware!
  [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
        nrepl-server-config]
    (println "add-middleware! nrepl port: " port)
    (let [state (client/connect! port)
          request! (partial client/request! state)]
      (println "connected!")
      (println "init results: "
               (map  (comp print-eval-result
                             ;status-e 
                             ;<!!
                           request!) ops-sniffer)))))