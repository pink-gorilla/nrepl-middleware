(ns demo.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [pinkgorilla.nrepl.service.nrepl-server :refer [run-nrepl-server]]
   ;[pinkgorilla.nrepl.service.add-middleware :refer [add-middleware!]]
   [pinkgorilla.nrepl.service.relay-jetty :refer [run-relay-jetty]])
  (:gen-class))

(def demo-config
  {:nrepl-server {:bind "127.0.0.1"
                  :port 9100}
   :relay {:port 9000
           :route "/nrepl"}})

(defn -main []

(timbre/set-config!
 (merge timbre/default-config
        {:min-level ;:info
         [[#{"pinkgorilla.nrepl.client.connection"} :debug]
          [#{"*"} :warn]]}))


  (run-nrepl-server demo-config)
  ;(add-middleware! demo-config) ; not needed in case of in process nrepl-server
  (run-relay-jetty demo-config))


