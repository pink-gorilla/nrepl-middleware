(ns pinkgorilla.nrepl.service.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [nrepl.server]
   [pinkgorilla.nrepl.middleware.cider :refer [cider-handler]]
   [picasso.default-config] ; side-effects   
   [picasso.datafy.file] ; side-effects
   ;[pinkgorilla.nrepl.sniffer.core] ; side-effects
   ))

(defn run-nrepl-server [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [port]} nrepl-server-config]
    (info "starting nrepl server at port " port "..")
    (let [nrepl-server (nrepl.server/start-server :port port
                                                  :handler (cider-handler))]
      nrepl-server)))

