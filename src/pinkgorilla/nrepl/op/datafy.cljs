(ns pinkgorilla.nrepl.op.datafy
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout close!]]
   [reagent.core :as r]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))

(defn ^:export nrepl-nav
  "evaluates a clj-snippet"
  [conn datafy-id k v]
  (nrepl-op-complete
   conn
   {:op "gorilla-nav"
    :datafy-id datafy-id
    :datafy-k k
    :datafy-v v}
   (fn [fragments]
     fragments)))