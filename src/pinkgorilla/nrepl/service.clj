(ns pinkgorilla.nrepl.service
  (:require
   [taoensso.timbre :as timbre :refer [debug infof error]]
   [pinkgorilla.nrepl.server.nrepl-server :refer [run-nrepl-server]]
   ;[pinkgorilla.nrepl.server.add-middleware :refer [add-middleware!]]
   ;[pinkgorilla.nrepl.relay.jetty :refer [run-relay-jetty]]
   [pinkgorilla.nrepl.relay.sente]))

#_(def default-config
    {:nrepl {:server {:bind "127.0.0.1"
                      :port 9100}
             :relay  {:host "127.0.0.1"
                      :port 9100
                  ;:transport-fn
                      }}
     :web {:port 9500
           :route "/api/nrepl"}})

(defn start-nrepl [app-config]
  (let [nrepl-config (merge {:enabled false
                             :bind "0.0.0.0"
                             :port 9100}
                            (or (get-in app-config [:nrepl]) {}))]
    (if (:enabled nrepl-config)
      (do
        (infof "starting nrepl on %s:%s" (:bind nrepl-config) (:port nrepl-config))
        (run-nrepl-server nrepl-config))
      (warn "nrepl is disabled."))
  ;(add-middleware! demo-config) ; not needed in case of in process nrepl-server
  ;(run-relay-jetty default-config)
    ))
