(ns pinkgorilla.nrepl.client.connection.bencode
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
   [clojure.core.async :as async :refer [<! >! chan poll! timeout close! go go-loop]]
   [taoensso.timbre :refer [debug debugf info infof warn warnf errorf]]
   [nrepl.core :as nrepl]
   [nrepl.transport :as nt]
   [pinkgorilla.nrepl.client.id :refer [guuid]]))

; "close", which drops the session indicated by the ID in the :session slot. 
; The response message's :status will include :session-closed.

(defn disconnect! [{:keys [conn] :as s}]
  (let [transport (:transport @conn)]
    (info "disconnecting client nrepl session.")
    (swap! conn dissoc ;:transport 
           :client :connected?)
    ;(.close transport)
    ))

(defn- disconnect-t! [conn]
  (let [transport (:transport @conn)]
    (info "disconnecting client nrepl session.")
    (swap! conn dissoc :transport :client :connected?)
    (.close transport)))

; "interrupt", which will attempt to interrupt the current execution with id provided in the :interrupt-id slot.

(defn- set-session-id! [conn res]
   ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  (when-not (:session-id @conn)
    ;(when (= (:id res) id-req-clone)
    (if-let [id (:new-session res)]
      (do (infof "setting session id: %s" id)
          (swap! conn assoc :session-id id)
          true)
      (warnf "cannot set session id from res: %s" res))))

(defn connect! [config]
  (let [req-ch (chan)
        res-ch (chan)
        {:keys [port host transport-fn]
         :or {transport-fn nt/bencode
              host "127.0.0.1"}} config
        _ (infof "nrepl connecting %s:%s %s" host port transport-fn)
        t (nrepl/connect :port port
                         :host host
                         :transport-fn transport-fn)
        conn (atom {:req-ch req-ch ; core.async channel where to send nrepl messages to
                    :res-ch res-ch ; core.async channel to receive messages
                    :transport t
                    :connected? true
                    :session-id nil})
        id-req-clone (guuid)]
    (go
      (nt/send t {:op "clone" :id id-req-clone})
      ;(>! req-ch {:op "clone" :id id-req-clone})
      )

    (go-loop []
      (let [session-id (:session-id @conn)
            connected? (:connected? @conn)]

        ; req-ch -> nrepl
        (when session-id
          (when-let [req (poll! req-ch)]
            (let [req (if session-id (assoc req :session session-id) req)]
              (debugf "nrepl req  send: %s " req)
              (nt/send t req))))

        ; nrepl res -> res-ch
        (when-let [res (nt/recv t 5)] ; recv is blocking, after 5ms will return nil if no res rcvd
          (debugf "nrepl res rcvd: %s " res)

          (when-not session-id
            (set-session-id! conn res))

          (>! res-ch res))

        (if-not connected?
          (disconnect-t! conn)
          (recur))))

    ; return conn
    conn))
