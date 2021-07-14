(ns pinkgorilla.nrepl.relay.session
  (:require
   [clojure.core.async :refer [<! <!! >! go go-loop]]
   [taoensso.timbre :as timbre :refer [info infof warn]]
   [pinkgorilla.nrepl.client.connection :as cc :refer [connect!]]))

(defn create-session! [config ws-send!]
  (info "creating nrepl relay-session ..")
  (let [conn (connect! config)
        {:keys [:res-ch]} @conn]
    (go-loop []
      (let [res (<! res-ch)]
        (if res
          (do
            (infof "res ws: %s" res)
            (ws-send! res)
            (recur))
          (warn "no res recvd. nrepl conn lost?"))))
    conn))

(defn request! [conn req]
  (let [{:keys [req-ch]} @conn]
    (infof "req ws: %s" req)
    (go
      (>! req-ch req))))

(defn disconnect! [conn]
  (info "nrepl disconnect")
  (cc/disconnect! conn))


