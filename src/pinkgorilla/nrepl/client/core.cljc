(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop]]
      :cljs [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]])
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn]]
      :clj [taoensso.timbre :refer [debug info warn]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   ; side-effects (register multi-methods)
   [pinkgorilla.nrepl.client.op.eval]
   [pinkgorilla.nrepl.client.op.concat]
   [pinkgorilla.nrepl.client.op.cider]
   [pinkgorilla.nrepl.client.op.admin]

   [pinkgorilla.nrepl.client.connection :refer [connect!]]
   [pinkgorilla.nrepl.client.multiplexer :refer [create-multiplexer!]]
   [pinkgorilla.nrepl.client.request :as r]))

(defn- process-fragment-log [process-fragment result fragment]
  (let [r (process-fragment result fragment)]
    (info "processing: " fragment "result: " r)
    r))

(defn process-req [{:keys [req fragments]}]
  (let [{:keys [initial-value process-fragment]} (init req)
        p (partial process-fragment-log process-fragment)]
    (reduce p initial-value fragments)))


(defn connect [config]
  (let [conn (connect! config)
        mx (create-multiplexer! conn)]
    {:config config
     :conn conn
     :mx mx}))

(defn send-request! [{:keys [conn mx]} req & [partial-results?]]
  (r/send-request! conn mx partial-results? req))

(defn send-requests!
  [s reqs]
  (go-loop [todo reqs]
    (when-let [req (first todo)]
      (send-request! s req false)
      (recur (rest todo)))))

(defn send-request-sync!
  [s req]
  (let [result-chan (send-request! s req false)]
    (let [r (<! result-chan)]
      (info "result: " r)
      )
    ))

