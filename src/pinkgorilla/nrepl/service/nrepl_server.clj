(ns pinkgorilla.nrepl.service.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.cider :refer [cider-handler]]
   [picasso.default-config] ; side-effects   
   [picasso.datafy.file] ; side-effects
   ))

(defn run-nrepl-server [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
         nrepl-server-config]
    (info "nrepl server starting at " (str bind ":" port) "..")
    (let [nrepl-server (nrepl.server/start-server :bind bind
                                                  :port port
                                                  :handler (cider-handler))]
      nrepl-server)))

