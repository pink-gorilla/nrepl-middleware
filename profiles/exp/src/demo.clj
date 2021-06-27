(ns demo
  (:require
   [taoensso.timbre :as timbre :refer [debug info error]]
   [clojure.core.async :refer [chan go go-loop <! >!]]
   [pinkgorilla.nrepl.relay.async :refer [make-relay]]
   [pinkgorilla.nrepl.data.demo-ops :as demo])
  (:gen-class))


(defn exec [req-ch ops]
  (doall (for [r ops]
           (do (println "req: " r)
               (go (>! req-ch r))))))

(defn -main [& args]

  (timbre/set-config!
   (merge timbre/default-config
          {:min-level ;:info
           [;[#{"pinkgorilla.nrepl.client.connection"} :debug]
            [#{"*"} :info]]}))

  (let [conn (make-relay)
        {:keys [req-ch res-ch]} @conn

        ops (concat demo/op-eval-simpel
                    demo/op-eval-sniffer
                    demo/op-eval-sniffer-picasso
                    demo/op-eval-ex
                    demo/op-nrepl
                    demo/op-cider)]

    (go-loop [res (<! res-ch)]
      (println "res: " res)
      (recur (<! res-ch)))

    (go (exec req-ch ops))

    (println "wunderbar!")))

