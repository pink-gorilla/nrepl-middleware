(ns pinkgorilla.nrepl.client.connection.in-process
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [clojure.core.async :refer [chan go go-loop <! >!]]
   [nrepl.transport]
   [nrepl.core]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]))

(defn connect [config]
  (let [req-ch (chan)
        res-ch (chan)
        handler (make-default-handler)
        _  (info "starting nrepl in-process async connection..")
        transport (nrepl.transport/piped-transports)
        [read write] transport
        timeout Long/MAX_VALUE
        client (nrepl.core/client read timeout)
        replies-seq (client)
        conn (atom {:req-ch req-ch ; core.async channel where to send nrepl messages to
                    :res-ch res-ch ; core.async channel to receive messages
                    :connected? true
                    :session-id nil
                    :do-recur true})]

    (go-loop [msg-req (<! req-ch)]
      (nrepl.server/handle* msg-req handler write)
      (when (:do-recur @conn)
        (recur (<! req-ch))))

    (go (loop [s replies-seq]
          (let [msg (first s)]
            (>! res-ch msg)
            (when (:do-recur @conn)
              (recur (rest s))))))

    conn))

(defn disconnect! [{:keys [conn]}]
  (info "disconnecting in-process nrepl session.")
  (swap! conn assoc :do-recur false))
