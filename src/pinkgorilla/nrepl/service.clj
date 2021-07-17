(ns pinkgorilla.nrepl.service
  (:require
   [taoensso.timbre :as timbre :refer [debug infof warn error]]
   [pinkgorilla.nrepl.server.nrepl-server :refer [run-nrepl-server]]
   ;[pinkgorilla.nrepl.server.add-middleware :refer [add-middleware!]]
   [pinkgorilla.nrepl.server.relay-sente :refer [start-sente-relay]]))

; service brings relay-sente to scope.
(defn start-nrepl [config]
  (run-nrepl-server config)
  (start-sente-relay)
  ;(add-middleware! demo-config) ; not needed in case of in process nrepl-server
  )
