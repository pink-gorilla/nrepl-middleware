(ns pinkgorilla.nrepl.server.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]))

(defn run-nrepl-server [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
        nrepl-server-config]
    (info "nrepl server starting at " (str bind ":" port) "..")
    (let [nrepl-server (nrepl.server/start-server :bind bind
                                                  :port port
                                                  :handler (make-default-handler))]
      nrepl-server)))

