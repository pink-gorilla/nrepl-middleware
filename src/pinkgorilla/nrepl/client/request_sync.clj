(ns pinkgorilla.nrepl.client.request-sync
  (:require
   [clojure.pprint :refer [pprint]]
   [nrepl.core :as nrepl]))

(defn- set-session-id! [conn fragments]
   ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  (when-not (:session-id @conn)
    (when-let [f (first fragments)]
      (when-let [id (:new-session f)]
        (info "setting session id: " id)
        (swap! conn assoc :session-id id)))))

(defn connect-raw!
  "connects to nrepl server
   returns connection-state atom"
  [port host transport-fn]
  (let [transport (nrepl/connect :port port
                                 :host host
                                 :transport-fn transport-fn)  ; :host "172.18.0.5"
        client (nrepl/client transport Long/MAX_VALUE)  ; 15000 
        conn (atom {:transport transport
                    :client client
                    :session-id nil})
        clone-response (send-request-sync! conn {:op "clone"})]
     ;Uses :op clone to keep the same session for multiple requests.
    (set-session-id! conn clone-response)
    (pprint clone-response)
    conn))

(defn disconnect! [conn]
  (let [transport (:transport @conn)]
    (println "disconnecting client nrepl session.")
    (swap! conn dissoc :transport :client)
    (.close transport)))

(defn- add-session-id [state msg]
  (if-let [session-id (:session-id @state)]
    (assoc msg :session session-id)
    msg))

(defn send-request-sync!
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

(defn request-rolling!
  "make a nrepl request ´msg´ and for each partial reply-fragment
   execute ´fun´"
  [conn msg fun]
  (if-let [client (:client @conn)]
    (loop [fragments (client msg)]
      (let [f (take 1 fragments)]
        (fun f)
        (recur (rest fragments))))
    (println "cannot send nrepl msg. not connected!")))

