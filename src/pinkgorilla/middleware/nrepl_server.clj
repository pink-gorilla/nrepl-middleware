(ns pinkgorilla.middleware.nrepl-server
  (:require
    [nrepl.server :as srv]
    [pinkgorilla.middleware.handle :as mw]
    [cider.nrepl :as cider]))

(def nrepl (atom {}))

(defn start-nrepl!
  [port]
  (let [server (srv/start-server :port port
                                 :handler (mw/nrepl-handler false cider/cider-middleware))]
    (swap! nrepl assoc port server)
    server))

(defn stop-nrepl!
  [port]
  (let [server (get @nrepl port)]
    (srv/stop-server server)))
