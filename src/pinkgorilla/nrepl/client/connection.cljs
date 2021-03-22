(ns pinkgorilla.nrepl.client.connection
  "A nrepl websocket client
   that passes messages back and forth to an already 
   running nREPL server."
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]]
   [taoensso.timbre :refer-macros [tracef debug debugf info warn error]]
   [reagent.core :refer [atom]]
   [chord.client :as chord] ; websockets with core.async
   [pinkgorilla.nrepl.client.id :refer [guuid]]))

(defn- reply-msg [msg reply]
  (merge {:id (:id msg)
          :status #{:done}} reply))

(defn- connection-msg [session-status]
  {:id (guuid)
   :session session-status
   :status :done})

(defn- process-incoming-nrepl-msgs
  [conn]
  (let [{:keys [session-id ws-ch res-ch connected?]} @conn
        fail-fn (fn [error-msg]
                  (error "closing ws after error:" error-msg)
                  (close! ws-ch))]
    (info "processing incoming ws nrepl messages ..")
    (go-loop [got-session-msg false]
      (let [{:keys [message error]} (<! ws-ch)] ; data is nil when ws session gets disconnected.
        (if (or error (nil? message))
          (fail-fn "nrepl websocket session was disconnected!")
          (do
            (debug "res nrepl: " message)
            (if got-session-msg
              (do
                (>! res-ch message)
                (recur true))
              (if-let [new-session-id (:new-session message)]
                (do
                  (info "nrepl: Got session-id msg " message)
                  (swap! conn assoc
                         :connected? true
                         :session-id new-session-id)
                  (>! res-ch (connection-msg :connected))
                  (recur true))
                (do
                  (error "waiting for :new-session. dumping: " message)
                  (recur false))))))))))

(defn- process-user-messages [conn]
  (go-loop []
    (let [{:keys [session-id ws-ch res-ch req-ch connected?]} @conn]
      (if connected?
        (if-let [message (<! req-ch)]
          (do
            (tracef "req user: %s" message)
            (if (and ws-ch connected?)
              (let [m2 (merge message {:session session-id})]
                (debugf "req nrepl send: %s" m2)
                (>! ws-ch m2))
              (>! res-ch (reply-msg message {:error "Rejected - no websocket connection"}))))
          (error "user req: received empty req (chan closed?)"))
        (do (debug "waiting until ws connected. not yet processing user reqs.")
            (<! (timeout 1000))))
      (recur))))

(defn connect!
  "creates an nrepl connection via websocket.
   input:
   ws-url       url to establish the websocket with  
   output: 
   {:req-ch   core.async channel where to send nrepl messages to
    :res-ch  core.async channel to receive messages 
    :connected? reagent atom, true when connection established,
                and ready to receive requests (is set after op: clone)
   }
   "
  [{:keys [ws-url]}]
  (let [req-ch (chan)
        res-ch (chan)
        conn (atom {:session-id nil  ; sent from nrepl on connect, set by receive-msgs!
                    :req-ch req-ch
                    :res-ch res-ch
                    :connected? false
                    :ws-ch nil})
        id-req-clone (guuid)]
    (go-loop [connected-prior? false]
      (info "nrepl ws connecting to " ws-url)
      (let [{:keys [ws-channel error]} (<! (chord/ws-ch ws-url {:format :edn}))]
        (if error
          (do
            (error "nrepl ws-chan connection failed!")
            (swap! conn assoc
                   :connected? false
                   :ws-ch nil)
            (when connected-prior?
              (>! res-ch (connection-msg :disconnected)))
            (<! (timeout 3000))
            (recur false))
          (do
            (info "nrepl ws-chan connected!")
            (swap! conn assoc :ws-ch  ws-channel)
            ;(when-not connected-prior?
            (>! ws-channel {:op "clone" :id id-req-clone}) ; )
            (<! (process-incoming-nrepl-msgs conn))
            (error "nrepl incoming processing finished (ws closed?)")
            (<! (timeout 3000))
            (recur true)))))
     ; process incoming user messages
    (process-user-messages conn)
    conn))

(defn disconnect! [conn]
 ; (let [transport (:transport @conn)]
  (info "disconnecting client nrepl session. not implemented."))