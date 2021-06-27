(ns pinkgorilla.nrepl.relay.async
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [clojure.core.async :refer [chan go go-loop <! >!]]
   [nrepl.transport]
   [nrepl.core]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]))

(defn make-relay []
  (let [req-ch (chan)
        res-ch (chan)
        handler (make-default-handler)
        _  (println "nrepl server running..")
        transport (nrepl.transport/piped-transports)
        [read write] transport
        timeout Long/MAX_VALUE
        client (nrepl.core/client read timeout)
        replies-seq (client)
        conn (atom {:req-ch req-ch ; core.async channel where to send nrepl messages to
                    :res-ch res-ch ; core.async channel to receive messages
                    :connected? true
                    :session-id nil})]

    (go-loop [msg-req (<! req-ch)]
      (nrepl.server/handle* msg-req handler write)
      (recur (<! req-ch)))

    (go (loop [s replies-seq]
          (let [msg (first s)]
            (>! res-ch msg)
            (recur (rest s)))))

    conn))

