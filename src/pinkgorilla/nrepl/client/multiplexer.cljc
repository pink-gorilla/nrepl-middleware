(ns pinkgorilla.nrepl.client.multiplexer
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debugf errorf]]
      :clj [taoensso.timbre :refer [debugf errorf]])
   #?(:clj [clojure.core.async :as async :refer [<! go-loop]]
      :cljs [cljs.core.async :as async :refer [<!] :refer-macros [go-loop]])
   #?(:cljs [reagent.core :refer [atom]])))

(defn- log-msg [msg]
  (debugf "Multiplexer process res: %s" msg))

(defn- process-resp [mx res]
  (let [req-id (:id res)
        req-id (if (keyword? req-id) req-id (keyword req-id))
        request-processor (get @mx req-id)]
    (if request-processor
      (if-let [p (:process-response request-processor)]
        (p res)
        (errorf "no response processing fn: %s" request-processor))
      (errorf "no req-processor registered for req-id: %s resp: %s" req-id res))))

(defn create-multiplexer!
  "reads from nrepl-resp-chan in a go-loop
   calls req-processors for processing"
  [conn]
  (let [mx (atom {}) ; keys: request-id, vals: request-state
        res-ch (:res-ch @conn)]
    ; process incoming responses from nrepl-chan
    (go-loop []
      (let [res (<! res-ch)] ; read incoming responses from nrepl channel]
        (log-msg res)
        (process-resp mx res)
        (recur)))
    mx))


