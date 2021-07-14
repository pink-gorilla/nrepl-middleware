(ns relay
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [pinkgorilla.nrepl.server.nrepl-server :refer [run-nrepl-server]]
   ;[pinkgorilla.nrepl.server.add-middleware :refer [add-middleware!]]
   [pinkgorilla.nrepl.relay.jetty :refer [run-relay-jetty]])
  (:gen-class))

(def demo-config
  {:nrepl {:server {:bind "127.0.0.1"
                  :port 9100}
           :relay  {:host "127.0.0.1"
                   :port 9100
                  ;:transport-fn
                  }}
   :web {:port 9500
         :route "/api/nrepl"}})


(defn -main []

  (timbre/set-config!
   (merge timbre/default-config
          {:min-level ;:info
           [;[#{"pinkgorilla.nrepl.client.connection"} :debug]
            [#{"*"} :info]]}))

  (run-nrepl-server (get-in demo-config [:nrepl :server]))
  ;(add-middleware! demo-config) ; not needed in case of in process nrepl-server
  (run-relay-jetty demo-config))

