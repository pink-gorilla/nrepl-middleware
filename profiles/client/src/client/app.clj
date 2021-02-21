(ns client.app
  (:require
   [clojure.core.async :refer [<! <!! go go-loop]]
   ;[taoensso.timbre :as timbre :refer [info]]
   [pinkgorilla.nrepl.client :as client]
   [pinkgorilla.nrepl.helper :refer [print-fragments status success?]])
  (:gen-class))

; (timbre/set-level! :trace) ; Uncomment for more logging
; (timbre/set-level! :debug)
; (timbre/set-level! :info)


(def ops-ide [{:op "describe"}
              {:op "ls-sessions"}
              {:op "ls-middleware"}
              {:op "eval" :code "(+ 1 1)"}

              {:op "sniffer-status"}
              {:op "eval" :code ":gorilla/sniff-on"} 
              {:op "sniffer-source"}  ; this starts sniffing on this session

              ; this ops get forwarded
              {:op "eval" :code "(+ 2 2)"}
              {:op "eval" :code "^:R [:p/vega (+ 8 8)]"}
              {:op "eval" :code "(time (reduce + (range 1e6)))"}
              ;{:op "pinkieeval" :code "^:R [:p (+ 8 8)]"}
              ])

(defn eval [state msg]
  (println "\r\n" msg) 
  (->> msg 
        (client/request! state)
       print-fragments))

(defn -main [& args]
  (let [port 9100
        [mode] args
        ;_ (println "args:" args "mode:" mode)
        _ (println "nrepl-client: connecting to nrepl server at port" port)
        state (client/connect! port)
        eval (partial eval state)]
    (case mode
      "sink" 
      (println (client/request-rolling! state {:op "sniffer-sink"} println))

      "ide"
      (doall (map eval ops-ide))
      
      ; else:
      (do (println "To listen (notebook mode): lein client listen")
          (println "To eval (ide mode: lein client ide)")))
    (client/disconnect! state)))
