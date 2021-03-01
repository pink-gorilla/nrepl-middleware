(ns client.app
  (:require
   [clojure.core.async :refer [<! <!! go go-loop]]
   [taoensso.timbre :as timbre :refer [info]]
   ;[pinkgorilla.nrepl.client.request-sync :refer [request-rolling!]]
   [pinkgorilla.nrepl.helper :refer [print-fragments status success?]]
   [pinkgorilla.nrepl.client.core :refer [connect send-request-sync!]] ; side effects
   )
  (:gen-class))

; (timbre/set-level! :trace) ; Uncomment for more logging
;(timbre/set-level! :debug)
(timbre/set-level! :info)
(timbre/set-level! :warn)


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
         (println "yeah: ")))

(defn print-forwarded [msg]
  (let [msg-forward (:sniffer-forward (first msg))]
    (if msg-forward
      (println msg-forward)
      (println msg))))

(defn -main [& args]
  (let [config {:port 9100}
        [mode] args
        ;_ (println "args:" args "mode:" mode)
        ;_ (println "nrepl-client: connecting to nrepl server at port" (:port config))
        conn (when (or (= mode "sink") (= mode "ide")) (connect config))
        neval (partial neval conn)
        ]
    (case mode
      "sink"
     ; (println (request-rolling! conn {:op "sniffer-sink"} print-forwarded))
      (println "bongo")
      
      "ide"
      (do
        (doall (map neval ops-ide)) ; blocking
        ;(send-requests! conn (take 2 ops-ide)) ; 
        (read-line)
        (println "quit.")
        ;(disconnect! conn)
        )

      ; else:
      (do (println "To listen (notebook mode): lein client listen")
          (println "To eval (ide mode): lein client ide")))))
