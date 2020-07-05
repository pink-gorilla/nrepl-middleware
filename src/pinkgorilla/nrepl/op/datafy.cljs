(ns pinkgorilla.nrepl.op.datafy
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout close!]]
   [reagent.core :as r]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))

(defn ^:export nrepl-nav
  [conn idx k v]
  (nrepl-op-complete
   conn
   {:op "gorillanav"
    :datafy (pr-str {:idx idx
                     :k k
                     :v v})}
   (fn [fragments]
     (into [] (map :datafy fragments))
     fragments)))