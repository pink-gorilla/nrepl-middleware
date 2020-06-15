(ns pinkgorilla.nrepl.server.core
  (:require
   [nrepl.server :refer [start-server stop-server]]
   [cider.nrepl :as cider]
   [pinkgorilla.middleware.handle :refer [nrepl-handler]]))

(def nrepl (atom {}))

(defn start-nrepl!
  [port]
  (let [server (start-server :port port
                             :handler (nrepl-handler false cider/cider-middleware))]
    (swap! nrepl assoc port server)
    server))

(defn stop-nrepl!
  [port]
  (let [server (get @nrepl port)]
    (stop-server server)))
