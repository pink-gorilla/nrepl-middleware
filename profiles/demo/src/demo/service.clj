(ns demo.service
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [pinkgorilla.nrepl.service :refer [start-nrepl]]
   [webly.config :refer [get-in-config]]))

;(info "starting nrepl service..")
(let [nrepl-config (get-in-config [:nrepl])]
  (start-nrepl nrepl-config))