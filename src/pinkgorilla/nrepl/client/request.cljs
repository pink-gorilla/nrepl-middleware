(ns pinkgorilla.nrepl.client.request
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!]]
   [taoensso.timbre :refer-macros [debug debugf info warn error errorf]]
   [cljs-uuid-utils.core :as uuid]
   [pinkgorilla.nrepl.client.connection]))

(defn nrepl-op
  "make-request 
   - sends `message` to websocket (so nrepl/cider can process the request)
   - returns the eval id.
   parameter:
   - state: this gets returned by (ws-start!)
   - message: a nrepl message (with or without request-id)
   - callback: optional callback that return all fragments of a request
   returns:
   - channel with response fragments"
  ([conn message]
   (let [request-ch (chan)
         {:keys [requests input-ch]} conn
         request-id (or (:id message) (uuid/uuid-string (uuid/make-random-uuid)))
         nrepl-msg  (merge message {:id request-id})]
     (swap! requests assoc (keyword request-id) request-ch)
     (debug "ws sending ws message: " nrepl-msg)
     (go
       (>! input-ch nrepl-msg))
     request-ch)))

(defn nrepl-op-complete
  ([conn msg]
   (nrepl-op-complete conn msg nil))
  ([conn msg transform-fn]
   (let [result-ch (chan)
         fragments (atom [])
         fragments-ch (nrepl-op conn msg)
         result-fn (fn [] (if transform-fn
                            (transform-fn @fragments)
                            @fragments))]
     (go-loop [msg (<! fragments-ch)]
       (if msg
         (do (swap! fragments conj msg)
             (recur (<! fragments-ch)))
         (do (>! result-ch (result-fn))
             (close! fragments-ch))))
     result-ch)))
