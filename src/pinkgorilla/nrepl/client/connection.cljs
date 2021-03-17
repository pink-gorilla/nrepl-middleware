(ns pinkgorilla.nrepl.client.connection
  "A nrepl websocket client
   that passes messages back and forth to an already 
   running nREPL server."
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]]
   [taoensso.timbre :refer-macros [debug info warn error]]
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
                  (error "ws error:" error-msg)
                  (close! ws-ch))]
    (info "processing incoming nrepl messages ..")
    (go-loop [got-session-msg false]
      (let [data (<! ws-ch)] ; data is nil when ws session gets disconnected.
        (if data
          (let [{:keys [message error]} data]
            (debug "NREPL RCVD: " data)
            (if error
              (fail-fn error)
              (if got-session-msg
                (do
                  (>! res-ch message)
                  (recur true))
                (if-let [new-session-id (:new-session message)]
                  (do
                    (info "Got session-id msg " message)
                    (swap! conn assoc
                           :connected? true
                           :session-id new-session-id)
                    (>! res-ch (connection-msg :connected))
                    (recur true))
                  (do
                    (error "waiting for :new-session. dumping: " message)
                    (recur false))))))
          (fail-fn "nrepl websocket session was disconnected!"))))))

(defn- process-user-messages [conn]
  (go-loop [first true]
    (let [{:keys [session-id ws-ch res-ch req-ch connected?]} @conn]
      (if connected?
        (if-let [message (<! req-ch)]
          (do
            (debug "USER RCVD: " message)
            (if (and ws-ch connected?)
              (let [m2 (merge message {:session session-id})]
                (debug "NREPL SEND: " m2)
                (>! ws-ch m2))
              (>! res-ch (reply-msg message {:error "Rejected - no websocket connection"}))))
          (error "USER RCVD: no data!"))
        (do (debug "not processing user msgs .. not connected")
            (<! (timeout 1000))))
      (recur false))))

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
                    :ws-ch nil})]
    (go-loop [connected-prior? false]
      (info "ws-connecting...")
      (let [{:keys [ws-channel error]} (<! (chord/ws-ch ws-url {:format :edn}))]
        (if error
          (do
            (error "ws-chan connection failed!")
            (swap! conn assoc :connected? false)
            (swap! conn assoc :ws-ch nil)
            (when connected-prior?
              (>! res-ch (connection-msg :disconnected)))
            (<! (timeout 3000))
            (recur false))
          (do
            (info "ws-chan connected!")
            (swap! conn assoc :ws-ch  ws-channel)
            ;(when-not connected-prior?
            (>! ws-channel {:op "clone" :id "uiui"}) ; )
            (<! (process-incoming-nrepl-msgs conn))
            (info "incoming nrepl processing finished (ws closed?)")
            (<! (timeout 3000000))
            (recur true)))))
     ; process incoming user messages
    (process-user-messages conn)
    conn))

(defn disconnect! [conn]
 ; (let [transport (:transport @conn)]
  (info "disconnecting client nrepl session. not implemented."))