(ns relay
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [webly.config :refer [get-in-config]]
   [pinkgorilla.nrepl.service :refer [start-nrepl]]))

(defn start []
   (start-nrepl (get-in-config [:nrepl])))
