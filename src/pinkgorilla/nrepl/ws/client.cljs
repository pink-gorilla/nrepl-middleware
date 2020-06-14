(ns pinkgorilla.nrepl.ws.client
  "A nrepl websocket client
   that passes messages back and forth to an already 
   running nREPL server."
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [taoensso.timbre :refer [debug info warn error]]
   [cljs-uuid-utils.core :as uuid]
   [chord.client :refer [ws-ch]])) ; websockets with core.async

(defn- filtered-chan!
  "takes the global fragment channel, and creates a new channel
   that only contains response (fragments) related to the request.
   This is useful for applications that want to display partial 
   responses (say console output) during a longer running evaluation)"
  [state request-id callback]
  (let [fragment-ch (:fragment-ch state)
        filtered-ch (chan)
        fragments (atom [])]
    (go-loop []
      (let [msg (<! fragment-ch)
            id (:id msg)]
        (when (= id request-id)
          (>! fragment-ch msg)
          (swap! fragments conj msg)
          (if (contains? (:status msg) :done) ;; eval status
            (do (close! filtered-ch)
                (when callback
                  (callback {:id request-id :fragments @fragments})))
            (recur)))))
    filtered-ch))


(defn make-request!
  "make-request 
   - sends `message` to websocket (so nrepl/cider can process the request)
   - returns the eval id.
   parameter:
   - state: this gets returned by (ws-start!)
   - message: a nrepl message (with or without request-id)
   - callback: optional callback that return all fragments of a request
   returns:
   - cannel with response fragments"
  ([state message]
   (make-request! state message nil))
  ([state message callback]
   (let [ws-chan @(:ws-chan state)
         request-id (or (:id message) (uuid/uuid-string (uuid/make-random-uuid)))
         session-id (:session-id state)
         nrepl-msg  (merge message {:id request-id :session @session-id})
         filtered-ch (filtered-chan! state request-id callback)]
     (info "ws sending ws message: " nrepl-msg)
     (if ws-chan
       (go
         (>! ws-chan nrepl-msg))
       (error "Cannot send nrepl message - ws not connected!"))
     filtered-ch)))

(defn- process-msg
  "processes an incoming message from websocket that comes from nrepl (and has cider enhancements)
   dispatches events to reagent to update notebook state ui."
  [state message]
  (let [result-ch (:result-ch state)
        fragment-ch (:fragment-ch state)
        ; this logging needs to be off when working with notebooks (slows them down)
        ; _ (info "ws rcvd message: " (pr-str message))
        {:keys [id status]} message
        request-id (keyword id)]
    (info "ws rcvd message request-id " request-id)
    (go
      (>! fragment-ch message)
      (when (contains? status :done) ;; eval status
        (do
          (swap! (:requests state) dissoc request-id)
          (>! result-ch message))))))

(defn- notify [state msg-type payload]
  (let [control-ch (:control-ch state)]
    (go
      (>! control-ch {:msg-type msg-type
                      :patload payload}))))

(defn- receive-msgs!
  [state]
  (let [ws-chan @(:ws-ch state)
        fail-fn (fn [error]
                  (close! ws-chan)
                  (dissoc state :ws-chan)
                  (notify state :session-disconnect error))]
    (go
      (let [{:keys [message error]} (<! ws-chan)]
        (if message
          (do
            (info "Got initial message " message)
            (if-let [new-session (:new-session message)]
              (do
                (swap! state assoc :session-id new-session)
                (notify state :session-connect {:message "Nrepl connection established!"}))
              (error "could not extract session id!!! "))
            (go-loop []
              (let [{:keys [message error]} (<! ws-chan)]
                (if message
                  (do
                    (process-msg state message)
                    (recur))
                  (fail-fn error)))))
          (fail-fn error))))))


(defn ws-start!
  [ws-url]
  (let [state {:ws-url ws-url
               :control-ch (chan)
               :fragment-ch (chan)
               :result-ch (chan)
               :ws-chan (atom nil)
               :session-id (atom nil) ; sent from nrepl on connect, set by receive-msgs!
               :requests (atom {}) ; callbacks of nrepl requests
               }
        msg-ch (:msg-ch state)]
    (go-loop [new-session true]
      (let [{:keys [ws-channel error]} (<! (ws-ch ws-url {:format :edn}))]
        (if-not error
          (do
            (reset! (:ws-chan state) ws-channel)
            (when new-session
              (go (>! msg-ch {:op "clone"})))
            (receive-msgs! state)
            (loop []
              (when-let [msg (<! msg-ch)]
                (>! ws-channel msg)
                (recur)))
            (<! (timeout 3000))
            (recur false))
          (let [session-id @(:session-id state)]
            (notify state :session-disconnect {:error error :session-id session-id})
            (<! (timeout 3000))
            (recur (nil? session-id))))))
    state))



