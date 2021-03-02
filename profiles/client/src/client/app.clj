(ns client.app
  (:require
   [clojure.core.async :refer [<! <!! go go-loop]]
   [taoensso.timbre :as timbre :refer [info]]
   ;[pinkgorilla.nrepl.client.request-sync :refer [request-rolling!]]
   [pinkgorilla.nrepl.helper :refer [print-fragments status success?]]
   [pinkgorilla.nrepl.client.core :refer [connect disconnect send-request-sync! request-rolling!]] ; side effects
   ;[pinkgorilla.nrepl.client.connection :refer [connect!]]
   ;[pinkgorilla.nrepl.client.request :refer [request-rolling!]]
   )
  (:gen-class))

(timbre/set-config!
 (merge timbre/default-config
        {:min-level ;:info
         [;[#{"pinkgorilla.nrepl.client.connection"} :debug]
          ;[#{"pinkgorilla.nrepl.client.op.eval"} :debug]
          [#{"*"} :warn]]}))


(def ops-ide [{:op "describe"}
              {:op "ls-sessions"}
              {:op "ls-middleware"}
              {:op "eval" :code "(+ 1 1)"}

              {:op "sniffer-status"}
              {:op "eval" :code ":gorilla/sniff-on"}
              {:op "sniffer-source"}  ; this starts sniffing on this session

              ; this ops get forwarded
              {:op "eval"  :code "^:X (+ 2 2)"}
              {:op "eval"  :code "^:R [:p/vega (+ 8 8)]"}
              {:op "eval"  :code "^:U (time (reduce + (range 1e6)))"}

              {:op "eval"  :code ":gorilla/off"}
              {:op "eval"  :code "\"NO\""}
              {:op "eval"  :code ":gorilla/on"}
              {:op "eval"  :code "\"YES\""}

              ; evals inside notebook would have this flag. check if it works:
              {:op "eval" :as-picasso 1 :code "^:R [:p (+ 8 8)]"}])

(defn neval [state msg]
  (println "\r\n" msg)
  (->> msg
       (send-request-sync! state)
       ;print-fragments
       (println "result: ")))

(defn print-forwarded [msg]

  (println ;"msg:" 
   msg))

(defn -main [& args]
  (let [config {:port 9100}
        [mode] args
        ;_ (println "args:" args "mode:" mode)
        ]
    (case mode
      "sink"
      (do
        (println "printing all sniffing results ... (exit with ctrl+c)")
        (-> (connect config)
            (request-rolling! {:op "sniffer-sink"} print-forwarded)))

      "ide"
      (let [conn (connect config)
            neval (partial neval conn)]
        (doall (map neval ops-ide)) ; blocking
        ;(send-requests! conn (take 2 ops-ide)) ; 
        ;(read-line)
        ;(println "quit.")
        (disconnect conn))

      ; else:
      (do (println "To listen (notebook mode): lein client sink")
          (println "To eval (ide mode):        lein client ide")))))
