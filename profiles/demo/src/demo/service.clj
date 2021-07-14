(ns demo.service
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [pinkgorilla.nrepl.service]))

;(info "starting nrepl service..")
(pinkgorilla.nrepl.service/start-nrepl
 {:nrepl {:bind "127.0.0.1"
          :port 9100}})