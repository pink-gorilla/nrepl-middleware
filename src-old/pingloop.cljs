


#_(defn- send-ping-loop
    "websocket connections have a timeout.
   we send regular ping events to keep connection alive"
    [{:keys [conn] :as c}]
    (go-loop []
      (let [{:keys [session-id ws-ch res-ch req-ch connected?]} @conn]
        (when connected?
          (debug "pinging ws-relay..")
          (send-request! c {:op "sniffer-status" :id (guuid)}))
        (<! (timeout 60000)) ; jetty default idle timeout is 300 seconds = 5 minutes
        (recur))))