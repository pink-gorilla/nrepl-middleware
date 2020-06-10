(ns pinkgorilla.kernel.nrepl
  (:require
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [nrepl.server]
   [nrepl.core]
   [pinkgorilla.middleware.cider :refer [cider-handler]]))

(def conn
  "open a single connection to the nREPL server 
   for the life of the application.
   It will be stored here."
  (atom nil))

(defn connect-to-nrepl
  "Connect to the nREPL server and store the connection."
  [host port]
  (println "kernel/nrepl connect to " host ":" port)
  (reset! conn (nrepl.core/connect :host host :port port)))

(defrecord NReplServer
           [handler server]
  component/Lifecycle
  (start [self]
    (let [config (get-in self [:config :config])
          {:keys [nrepl-port nrepl-host nrepl-port-file]} config]
      (if nrepl-port
        (if nrepl-host
          (do
            (log/info "Using nREPL at " nrepl-host ":" nrepl-port)
            (assoc self :remote-connection (connect-to-nrepl nrepl-host nrepl-port)))
          (do
            (log/info "Starting nREPL server on port " nrepl-port)
            (spit (doto nrepl-port-file .deleteOnExit) nrepl-port)
            (assoc self :server (nrepl.server/start-server :port nrepl-port
                                                           :handler (cider-handler)))))
        self)))
  (stop [self]
    (when server
      (nrepl.server/stop-server server)
      self)))

(defn new-cider-repl-server
  []
  (map->NReplServer {}))

