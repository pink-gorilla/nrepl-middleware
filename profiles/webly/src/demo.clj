(ns demo
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [nrepl.transport :as transport]
   [nrepl.core :as nrepl]
   [nrepl.server :as nrepl-server]
   [pinkgorilla.nrepl.server.nrepl-server :refer [run-nrepl-server]]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]])
  (:gen-class))

(def demo-config
  {:nrepl {:server {:bind "127.0.0.1"
                    :port 9100}
           :relay  {:host "127.0.0.1"
                    :port 9100
                  ;:transport-fn
                    }}
   :web {:port 9000
         :route "/api/nrepl"}})

(defn- process-replies
  [reply-fn contains-pred replies-seq]
  (loop [s replies-seq]
    (let [msg (first s)]
      (reply-fn msg)
      ;(when-not (contains-pred msg)
      (recur (rest s)))))
;)

(defn done? [res]
  (let [{:keys [status]} res
        done (or (contains? status :done) ;; res status
                 (some #(= "done" %) status))]
    ;(debugf "status: %s done: %s" status done)
    done))


(defn -main [& args]

  (timbre/set-config!
   (merge timbre/default-config
          {:min-level ;:info
           [;[#{"pinkgorilla.nrepl.client.connection"} :debug]
            [#{"*"} :info]]}))

  (let [server (run-nrepl-server (get-in demo-config [:nrepl :server]))
        handler (make-default-handler)
        _  (println "nrepl server running..")
        transport (transport/piped-transports)
        [read write] transport
        timeout Long/MAX_VALUE
        client (nrepl.core/client read timeout)
        msg {:op "eval" :code "(+ 7 7)"}]

    (future
      (nrepl.server/handle* msg handler write))
    (future
      (nrepl.server/handle* {:op "eval" :code "2"} handler write))

    (future
      (nrepl.server/handle* {:op "eval" :code "3"} handler write))

    (println "req sent.")
    ;(println (client))
    (process-replies println done? (client))



    (println "res. done.")))

