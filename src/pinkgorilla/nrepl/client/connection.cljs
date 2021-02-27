(ns pinkgorilla.nrepl.client.connection
  "A nrepl websocket client
   that passes messages back and forth to an already 
   running nREPL server."
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [taoensso.timbre :refer-macros [debug debugf info warn error errorf]]
   [reagent.core :as r]
   [pinkgorilla.nrepl.client.websocket :refer [ws-connect!]]))

#_(defn- filtered-chan!
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

(defn- chan-for-incoming-nrepl-msg
  "processes an incoming message that comes from channel (which comes 
   via websocket via websocket-relay from nrepl)
   Returns the corresponding request chan."
  [requests message]
  (let [; this logging needs to be off when working with notebooks (slows them down)
        ; _ (info "ws rcvd message: " (pr-str message))
        {:keys [id status]} message
        request-id (keyword id)
        request-ch (request-id @requests)]
    (debugf "%s fragment rcvd." request-id)
    (if request-ch
      (if (contains? status :done) ;; eval status
        (do
          (debugf "%s request done." request-id)
          (swap! requests dissoc request-id)
          [request-ch true])
        [request-ch false])
      [nil false])))

(defn- dump-msg [msg]
  (let [request-id (:id msg)]
    (errorf "%s dumping response. No associated request found: %s" request-id msg)))

(defn nrepl-client!
  "creates a nrepl connection via websocket
   Intended to be used with nrepl-op"
  [ws-url]
  (let [conn (ws-connect! ws-url)
        requests (r/atom {}) ; keys: request-id, vals: request-channel
        output-ch (:output-ch conn)]
    ; process incoming responses from nrepl
    (go-loop []
      (let [msg (<! output-ch)
            [req-ch done?] (chan-for-incoming-nrepl-msg requests msg)]
        (if req-ch
          (do (>! req-ch msg)
              (when done?
                (debugf "%s closing channel." (:id msg))
                (close! req-ch)))
          (dump-msg msg))
        (recur)))
    (assoc conn :requests requests)))


