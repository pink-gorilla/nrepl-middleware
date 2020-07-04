(ns pinkgorilla.nrepl.middleware.datafy
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [clojure.core.protocols]
   [clojure.datafy]
   [picasso.converter :refer [->picasso]]))


; https://github.com/RickMoynihan/nrebl.middleware
; https://github.com/Lokeh/punk
; https://github.com/pedro-w/nav-demo
; https://www.youtube.com/watch?v=c52QhiXsmyI&list=PLZdCLR02grLpMkEBXT22FTaJYxB92i3V3&index=3
; PREPL = Repl + presentation layer. klipse already supports this!!!


(def current (atom 0))

(defn next-id []
  (swap! current inc)
  @current)

(def items (atom {}))

(defn datafy-id [x]
  (when (satisfies? clojure.core.protocols/Datafiable x)
    (let [d (clojure.datafy/datafy x)
          id (next-id)]
      (info "datafy id: " id)
      (swap! items assoc id d)
      id)))

(defn nav! [id k v]
  (let [d (get @items id)]
    (clojure.datafy/nav d k v)))


