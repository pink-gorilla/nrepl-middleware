(ns pinkgorilla.nrepl.server.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [infof]]
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
   ;[pinkgorilla.nrepl.middleware.sniffer]
   ))

(defn run-nrepl-server [{:keys [bind port]
                         :or {bind "0.0.0.0"
                              port 9000}}]
  (infof "nrepl server starting at %s:%s .." bind port)
  (let [nrepl-server (nrepl.server/start-server :bind bind
                                                :port port
                                                :handler (make-default-handler))]
    nrepl-server))

