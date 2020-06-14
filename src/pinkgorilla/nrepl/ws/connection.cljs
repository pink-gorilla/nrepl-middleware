(ns pinkgorilla.nrepl.ws.connection
  "A nrepl websocket client
   that passes messages back and forth to an already 
   running nREPL server."
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [taoensso.timbre :refer [debug info warn error]]
   [cljs-uuid-utils.core :as uuid]
   [chord.client :as chord])) ; websockets with core.async


(defn- reply-msg [msg reply]
  (merge {:id (:id msg)
          :status :done} reply))

(defn- connection-msg [session-status]
  {:id (uuid/uuid-string (uuid/make-random-uuid))
   :session session-status
   :status :done})

(defn- process-incoming-nrepl-msgs
  [session-id ws-ch user-ch]
  (let [fail-fn (fn [error]
                  (error "ws error:" error)
                  (close! ws-ch))]
    (info "processing incoming nrepl messages ..")
    (go-loop [got-session-msg false]
      (let [{:keys [message error] :as data} (<! ws-ch)]
        (info "NREPL RCVD: " data)
        (if message
          (if got-session-msg
            (do
              (>! user-ch message)
              (recur true))
            (if-let [new-session-id (:new-session message)]
              (do
                (info "Got session-id msg " message)
                (reset! session-id new-session-id)
                (>! user-ch (connection-msg :connected))
                (recur true))
              (do
                (error "waiting for :new-session. dumping: " message)
                (recur false))))
          (fail-fn error))))))

(defn- process-user-messages [ws-ch user-ch session-id]
  (go-loop [first true]
    (if-let [message (<! user-ch)]
      (do
        (info "USER RCVD: " message)
        (if @ws-ch
          (let [m2 (merge message {:session @session-id})]
            (info "forwarding user msg to nrepl: " m2)
            (>! @ws-ch m2))
          (>! user-ch (reply-msg message {:error "Rejected - no websocket connection"}))))
      (error "USER RCVD: no data!"))
    (recur false)))

(defn ws-connect!
  [ws-url]
  (let [session-id (atom nil) ; sent from nrepl on connect, set by receive-msgs!
        user-ch (chan)
        ws-ch (atom nil)]
    (go-loop [connected? false]
      (info "ws-connecting...")
      (let [{:keys [ws-channel error]} (<! (chord/ws-ch ws-url {:format :edn}))]
        (if error
          (do
            (info "ws-chan connection failed!")
            (reset! ws-ch nil)
            (when connected?
              (>! user-ch (connection-msg :disconnected)))
            (<! (timeout 3000))
            (recur false))
          (do
            (info "ws-chan connected!")
            (reset! ws-ch ws-channel)
            (when-not connected?
              (>! ws-channel {:op "clone" :id "uiui"}))
            (<! (process-incoming-nrepl-msgs session-id ws-channel user-ch))
            (info "incoming nrepl processing finished (ws closed?)")
            (<! (timeout 3000))
            (recur false)))))
     ; process incoming user messages
    (process-user-messages ws-ch user-ch session-id)
    {:session-id session-id
     :ch user-ch}))



