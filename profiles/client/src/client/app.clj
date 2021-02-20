(ns client.app
  (:require
   [clojure.core.async :refer [<! <!! go go-loop]]
   ;[taoensso.timbre :as timbre :refer [info]]
   [pinkgorilla.nrepl.client :as client]
   [pinkgorilla.nrepl.helper :refer [print-eval-result status success?]])
  (:gen-class))

; (timbre/set-level! :trace) ; Uncomment for more logging
; (timbre/set-level! :debug)
; (timbre/set-level! :info)


(def ops-ide [{:op "describe"}
              {:op "ls-sessions"}
              {:op "ls-middleware"}

              {:op "eval" :code "(+ 1 1)"}

              {:op "sniffer-status-get"}
              {:op "sniffer-on"}

              {:op "eval" :code ":gorilla-remote-on"}
              {:op "eval" :code "(+ 2 2)"}
              {:op "eval" :code "^:R [:p/vega (+ 8 8)]"}
              {:op "eval" :code "(time (reduce + (range 1e6)))"}
              ;{:op "pinkieeval" :code "^:R [:p (+ 8 8)]"}
              ])

(defn -main [& args]
  (let [port 9100
        [mode] args
        ;_ (println "args:" args "mode:" mode)
        _ (println "nrepl-client: connecting to nrepl server at port" port)
        state (client/connect! port)
        request! (partial client/request! state)]
    (case mode
      "listen" (println (client/messages-print state {:op "sniffer-listen"}))
                  
      ;"listen" (let [c (client/exec-async! state {:op "sniffer-listen"})]
      ;           (println "waiting for remote evals..")
      ;           (go-loop []
      ;             (let [x (<!! c)]
      ;               (println (print-eval-result x)))
      ;             (recur)))
      "ide" (do
              (doall (map (comp print-eval-result request!) ops-ide)))
      (do (println "To listen (notebook mode): lein client listen")
          (println "To eval (ide mode: lein client ide)")))
    (client/disconnect! state)))
