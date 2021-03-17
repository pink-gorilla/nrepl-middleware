(ns pinkgorilla.nrepl.client.id
  #?(:cljs
     (:require
      [cljs-uuid-utils.core :as uuid-cljs])))

(defn guuid []
  #?(:clj (-> (.toString (java.util.UUID/randomUUID)))
     :cljs  (-> (uuid-cljs/make-random-uuid) uuid-cljs/uuid-string)))

(def current (atom 0))

(defn id []
  (swap! current inc)
  @current)
