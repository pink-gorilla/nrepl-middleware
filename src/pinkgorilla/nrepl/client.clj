(ns pinkgorilla.nrepl.client
  "a simple nrepl client.
   Uses :op clone to keep the same session for multiple requests.
   stateless implementation.
   After each req all eval res are pushed to callback-fn.
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


(defn- set-session-id! [state fragments]
  ;(println "set-session-id!")
  (when-not (:session-id @state)
    (when-let [f (first fragments)]
      (when-let [id (:new-session f)]
        (println "setting session id: " id)
        (swap! state assoc :session-id id)))))

(defn- add-session-id [state msg]
  (if-let [session-id (:session-id @state)]
    (assoc msg :session session-id)
    msg))

(defn process-responses [state on-receive-fn fragments]
  
  (on-receive-fn fragments)
  :nrepl-rep-rcvd)

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

(defn message!
  [state msg]
  (if-let [client (:client @state)]
    (do
      (println "nrepl-request: " msg)
      (->> msg
         ;(nrepl/message client)
           client
           doall))
    (println "cannot send nrepl msg. not connected!")))


(defn connect! [port]
  (let [transport (nrepl/connect :port port)  ; :host "172.18.0.5"
        client (nrepl/client transport Long/MAX_VALUE)  ; 15000 
        state (atom {:transport transport
                     :client client
                     :session-id nil})
        clone-response (request! state {:op "clone"})
        ]
    (set-session-id! state clone-response)    
    (pprint clone-response)
    state))

(defn disconnect! [state]
  (let [transport (:transport @state)]
    (println "disconnecting client nrepl session.")
    (swap! state dissoc :transport :client)
    (.close transport)))


(defn messages-print
  [state msg]
  (if-let [client (:client @state)]
    (loop [fragments (client msg)]
      (let [f (take 1 fragments)]
        (println f)
        (recur (rest fragments))))
    (println "cannot send nrepl msg. not connected!")))