(ns pinkgorilla.nrepl.client
  "a simple nrepl client.
  
   Usecase
     (connect! port) - returns state atom
     (send! state message)
     (send! state message) - so multiple calls possible
   
   todo: 
   - change bencode transport to edn transport?
   - take interface from system / nrepl ?
   - convert to async channels ?   "
  (:require
   [clojure.pprint :refer [pprint]]
   [nrepl.core :as nrepl]))

(defn- add-session-id [state msg]
  (if-let [session-id (:session-id @state)]
    (assoc msg :session session-id)
    msg))

(defn request!
  "makes a nrepl request.
   waits until all responses are received
   returns the fragments
   "
  [state msg]
  ;(println "send! msg" msg " state: " @state " fn: " on-receive-fn)
  (if-let [client (:client @state)]
    (->> (add-session-id state msg)
         (nrepl/message client)
         doall)
    (do
      (println "cannot send nrepl msg. not connected!")
      nil)))


(defn- set-session-id! [state fragments]
   ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  (when-not (:session-id @state)
    (when-let [f (first fragments)]
      (when-let [id (:new-session f)]
        (println "setting session id: " id)
        (swap! state assoc :session-id id)))))

(defn connect! 
  "connects to nrepl server
   returns connection-state atom"
  [port]
  (let [transport (nrepl/connect :port port)  ; :host "172.18.0.5"
        client (nrepl/client transport Long/MAX_VALUE)  ; 15000 
        state (atom {:transport transport
                     :client client
                     :session-id nil})
        clone-response (request! state {:op "clone"})
        ]
     ;Uses :op clone to keep the same session for multiple requests.
    (set-session-id! state clone-response)    
    (pprint clone-response)
    state))


; "close", which drops the session indicated by the ID in the :session slot. 
; The response message's :status will include :session-closed.

(defn disconnect! [state]
  (let [transport (:transport @state)]
    (println "disconnecting client nrepl session.")
    (swap! state dissoc :transport :client)
    (.close transport)))


(defn request-rolling!
  "make a nrepl request ´msg´ and for each partial reply-fragment
   execute ´fun´"
  [state msg fun]
  (if-let [client (:client @state)]
    (loop [fragments (client msg)]
      (let [f (take 1 fragments)]
        (fun f)
        (recur (rest fragments))))
    (println "cannot send nrepl msg. not connected!")))



  ; "interrupt", which will attempt to interrupt the current execution with id provided in the :interrupt-id slot.
