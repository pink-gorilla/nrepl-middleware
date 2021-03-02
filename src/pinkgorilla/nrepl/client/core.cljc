(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop <!!]]
      :cljs [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]])
   #?(:cljs [taoensso.timbre :refer-macros [debugf info infof]]
      :clj [taoensso.timbre :refer [debugf info infof]])

   ; side-effects (register multi-methods)
   [pinkgorilla.nrepl.client.op.eval]
   [pinkgorilla.nrepl.client.op.concat]
   [pinkgorilla.nrepl.client.op.cider]
   [pinkgorilla.nrepl.client.op.admin]
   [pinkgorilla.nrepl.client.op.gorilla]

   [pinkgorilla.nrepl.client.connection :refer [connect! disconnect!]]
   [pinkgorilla.nrepl.client.multiplexer :refer [create-multiplexer!]]
   [pinkgorilla.nrepl.client.request :as r]))

(defn connect [config]
  (let [conn (connect! config)
        mx (create-multiplexer! conn)]
    {:config config
     :conn conn
     :mx mx}))

(defn disconnect [s]
  (disconnect! (:conn s)))

(defn send-request! [{:keys [conn mx]} req & [partial-results?]]
  (let [result-ch (r/send-request! conn mx partial-results? req)]
    result-ch))

#_(defn send-requests!
    [s reqs]
    (go-loop [todo reqs]
      (when-let [req (first todo)]
        (send-request! s req false)
        (recur (rest todo)))))

#?(:clj

   (defn send-request-sync!
     " <!!  only works in clj. It is used to block until chan val arrives"
     [c req]
     (let [result-ch (send-request! c req false)
           r (<!! result-ch)]
       (infof "result: %s result-ch: " r result-ch)
       r))
 ;  
   )

(defn request-rolling!
  "make a nrepl request ´req´ and for each partial reply-fragment
   execute ´fun´"
  [c req fun]
  (if-let [result-ch (send-request! c req true)]
    (go-loop [result (<! result-ch)]
      (fun result)
      (recur (<! result-ch)))
    (info "cannot send nrepl msg. not connected!")))



