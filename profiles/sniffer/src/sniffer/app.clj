(ns sniffer.app
  (:require
   [clojure.core.async :refer [<! go]]
   [taoensso.timbre :as timbre :refer [info]]
   [pinkgorilla.nrepl.sniffer.core :refer [send!] :as sniffer]
   [pinkgorilla.nrepl.logger :refer [log-publish!]]
   [pinkgorilla.nrepl.sniffer.middleware :refer [chan-eval-results]])
  (:gen-class))

; (timbre/set-level! :trace) ; Uncomment for more logging
(timbre/set-level! :debug)
; (timbre/set-level! :info)

(defn -main [& args]
  (let [port 12000] ; port is specified in shadow-cljs.edn
    (println "sniffer: connecting to nrepl server at port" port)
    (sniffer/start-sniffer! port)
    (go (while true
          (let [n (<! chan-eval-results)]
            ; cannot print to console - this would capture 
            ; console output and would create a loop.
            (log-publish! n)
            ;(info "sniffed: " n)
        ;(publish-eval! n)
            )))
    (println "captured evals will be logged in target/")
    (println "now in your ide please do a few evals")
    (println "you might also want to 'lein ide'")))

;  (println "nrepl relay starting with cli-args: " args)
;  (run-nrepl-relay))

(comment

  (println "doing a few evals, just to see that nrepl works")
  (send! {:op "ls-sessions"})
  (send! {:op "eval" :code "(+ 8 8)"})
  (send! {:op "eval" :code "^:R [:p/vega (+ 8 8)]"})
  (send! {:op "eval" :code "(time (reduce + (range 1e6)))"})
  (send! {:op "pinkieeval" :code "^:R [:p (+ 8 8)]"})
  (send! {:op "start-rebl-ui"})


 ; 
  )