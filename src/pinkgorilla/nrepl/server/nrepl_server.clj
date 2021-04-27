(ns pinkgorilla.nrepl.server.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]
   ;; side effects
   [nrepl.middleware.print]
  ;picasso
   [picasso.default-config]
   [pinkgorilla.notebook.repl]
   [picasso.datafy.file]
  ; nrepl-miiddleware
   [pinkgorilla.nrepl.middleware.picasso]
   [pinkgorilla.nrepl.middleware.sniffer]))

(defn run-nrepl-server [nrepl-server-config]
  (let [{:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
        nrepl-server-config]
    (info "nrepl server starting at " (str bind ":" port) "..")
    (let [nrepl-server (nrepl.server/start-server :bind bind
                                                  :port port
                                                  :handler (make-default-handler))]
      nrepl-server)))

