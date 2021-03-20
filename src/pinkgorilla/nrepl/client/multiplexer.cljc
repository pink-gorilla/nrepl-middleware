(ns pinkgorilla.nrepl.client.multiplexer
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debugf errorf]]
      :clj [taoensso.timbre :refer [debugf errorf]])
   #?(:clj [clojure.core.async :as async :refer [<! go-loop]]
      :cljs [cljs.core.async :as async :refer [<!] :refer-macros [go-loop]])
   #?(:cljs [reagent.core :refer [atom]])))

(defn- log-msg [msg]
  (debugf "multiplexer res: %s" msg))

(defn req-id-kw [res]
  (let [req-id (:id res)
        req-id (if (keyword? req-id) req-id (keyword req-id))]
    req-id))

(defn- process-res [mx res]
  (let [req-id (req-id-kw res)
        request-processor (get @mx req-id)]
    (if request-processor
      (if-let [p (:process-response request-processor)]
        (p res)
        (errorf "no response processing fn: %s" request-processor))
      (errorf "no req-processor registered for req-id: %s resp: %s" req-id res))))

(defn create-multiplexer!
  "reads from nrepl-res-ch in a go-loop
   calls req-processors for processing"
  [conn]
  (let [mx (atom {}) ; keys: request-id, vals: request-state
        res-ch (:res-ch @conn)]
    ; process incoming responses from res-ch
    (go-loop []
      (let [res (<! res-ch)]
        (log-msg res)
        (process-res mx res)
        (recur)))
    mx))


