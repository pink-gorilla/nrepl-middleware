(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop <!!]]
      :cljs [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]])
   #?(:cljs [taoensso.timbre :refer-macros [debug debugf info infof warn]]
      :clj [taoensso.timbre :refer [debug debugf info infof warn]])

   ; side-effects (register multi-methods)
   [pinkgorilla.nrepl.client.op.eval]
   [pinkgorilla.nrepl.client.op.concat]
   [pinkgorilla.nrepl.client.op.cider]
   [pinkgorilla.nrepl.client.op.admin]

   [pinkgorilla.nrepl.client.connection :refer [connect!]]
   [pinkgorilla.nrepl.client.multiplexer :refer [create-multiplexer!]]
   [pinkgorilla.nrepl.client.request :as r]))



(defn connect [config]
  (let [conn (connect! config)
        mx (create-multiplexer! conn)]
    {:config config
     :conn conn
     :mx mx}))

(defn send-request! [{:keys [conn mx]} req & [partial-results?]]
  (let [result-ch (r/send-request! conn mx partial-results? req)]
    (debugf "send-request! result-ch: %s " result-ch)
    result-ch
    )
  )

#_(defn send-requests!
  [s reqs]
  (go-loop [todo reqs]
    (when-let [req (first todo)]
      (send-request! s req false)
      (recur (rest todo)))))

#?(:clj

   (defn send-request-sync!
     [c req]
     (let [result-chan (send-request! c req false)
           r (<!! result-chan)]
         (infof "result: %s result-chan: " r result-chan)
         r
         ))
 ;  
   )

