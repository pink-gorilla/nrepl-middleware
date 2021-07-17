(ns demo.conn
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre :refer-macros [debug info warn error]]
    ))



; this should be called by goldly on startup
; it requires that the config was loaded prior
;(rf/dispatch [:nrepl/init])

#_(rf/dispatch [:nrepl/connect  {:enabled true
                               :bind "127.0.0.1"
                               :port 9100}])