(ns demo.conn
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre :refer-macros [debug info warn error]]
   [cljs.core.async :as async :refer [<!] :refer-macros [go]]
   [pinkgorilla.nrepl.client.core :refer [connect]]
   [pinkgorilla.nrepl.kernel.subscriptions] ; side effects
   [pinkgorilla.nrepl.kernel.connection] ; side effects
   ))

(def config {:ws-url "ws://127.0.0.1:9500/api/nrepl"})


(rf/dispatch [:nrepl/connect-to  "ws://127.0.0.1:9500/api/nrepl"])

(def nrepl-status (rf/subscribe [:nrepl/status]))



;(def conn (connect config))
