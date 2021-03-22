(ns demo.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [pinkgorilla.nrepl.server.nrepl-server :refer [run-nrepl-server]]
   ;[pinkgorilla.nrepl.server.add-middleware :refer [add-middleware!]]
   [pinkgorilla.nrepl.relay.jetty :refer [run-relay-jetty]])
  (:gen-class))

(def demo-config
  {:nrepl-server {:bind "127.0.0.1"
                  :port 9100}
   :nrepl-client {:host "127.0.0.1"
                  :port 9100
                  ;:transport-fn
                  }
   :relay {:port 9000
           :route "/nrepl"}})


(defn -main []

  (timbre/set-config!
   (merge timbre/default-config
          {:min-level ;:info
           [[#{"pinkgorilla.nrepl.client.connection"} :debug]
            [#{"*"} :info]]}))

  (run-nrepl-server demo-config)
  ;(add-middleware! demo-config) ; not needed in case of in process nrepl-server
  (run-relay-jetty demo-config))


