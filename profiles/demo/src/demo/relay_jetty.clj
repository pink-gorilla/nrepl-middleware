(ns demo.relay-jetty
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [pinkgorilla.nrepl.service.nrepl-server :refer [run-nrepl-server]]
   [pinkgorilla.nrepl.service.relay-jetty :refer [run-relay-jetty]]
   [pinkgorilla.nrepl.client] ; side-effects
   )
  (:gen-class))

(def demo-config
  {:nrepl-server {:port 9100}
   :relay {:port 9000
           :route "/nrepl"}})

(defn -main []
  (timbre/set-level! :debug)
  (run-nrepl-server demo-config)
  (run-relay-jetty demo-config))


