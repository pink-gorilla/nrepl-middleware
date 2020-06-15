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
   [reagent.core :as r]
   [chord.client :as chord])) ; websockets with core.async


(defn- reply-msg [msg reply]
  (merge {:id (:id msg)
          :status :done} reply))

(defn- connection-msg [session-status]
  {:id (uuid/uuid-string (uuid/make-random-uuid))
   :session session-status
   :status :done})

(defn- process-incoming-nrepl-msgs
  [session-id ws-ch output-ch connected?]
  (let [fail-fn (fn [error-msg]
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
                  (>! output-ch message)
                  (recur true))
                (if-let [new-session-id (:new-session message)]
                  (do
                    (info "Got session-id msg " message)
                    (reset! connected? true)
                    (reset! session-id new-session-id)
                    (>! output-ch (connection-msg :connected))
                    (recur true))
                  (do
                    (error "waiting for :new-session. dumping: " message)
                    (recur false)))))))
        (fail-fn "nrepl websocket session was disconnected!")))))

(defn- process-user-messages [ws-ch input-ch output-ch session-id connected?]
  (go-loop [first true]
    (if-let [message (<! input-ch)]
      (do
        (debug "USER RCVD: " message)
        (if (and @ws-ch @connected?)
          (let [m2 (merge message {:session @session-id})]
            (debug "NREPL SEND: " m2)
            (>! @ws-ch m2))
          (>! output-ch (reply-msg message {:error "Rejected - no websocket connection"}))))
      (error "USER RCVD: no data!"))
    (recur false)))

(defn ws-connect!
  "creates an nrepl connection via websocket.
   input:
   ws-url       url to establish the websocket with  
   output: 
   {:input-ch   core.async channel where to send nrepl messages to
    :output-ch  core.async channel to receive messages 
    :connected? reagent atom, true when connection established,
                and ready to receive requests (is set after op: clone)
   }
   "

  [ws-url]
  (let [input-ch (chan)
        output-ch (chan)
        connected? (r/atom false)
        session-id (r/atom nil) ; sent from nrepl on connect, set by receive-msgs!
        ws-ch (atom nil)]
    (go-loop [connected-prior? false]
      (info "ws-connecting...")
      (let [{:keys [ws-channel error]} (<! (chord/ws-ch ws-url {:format :edn}))]
        (if error
          (do
            (error "ws-chan connection failed!")
            (reset! connected? false)
            (reset! ws-ch nil)
            (when connected-prior?
              (>! output-ch (connection-msg :disconnected)))
            (<! (timeout 3000))
            (recur false))
          (do
            (info "ws-chan connected!")
            (reset! ws-ch ws-channel)
            ;(when-not connected-prior?
            (>! ws-channel {:op "clone" :id "uiui"}) ; )
            (<! (process-incoming-nrepl-msgs session-id ws-channel output-ch connected?))
            (info "incoming nrepl processing finished (ws closed?)")
            (<! (timeout 3000))
            (recur true)))))
     ; process incoming user messages
    (process-user-messages ws-ch input-ch output-ch session-id connected?)
    {:session-id session-id
     :input-ch input-ch
     :output-ch output-ch
     :connected? connected?}))



