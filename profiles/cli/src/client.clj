(ns client
  (:require
   [clojure.core.async :refer [<! <!! go go-loop]]
   [clojure.tools.cli :as cli]
   [taoensso.timbre :as timbre :refer [info]]
   [clojure.core :refer [read-string]]
   [pinkgorilla.nrepl.helper :refer [print-fragments status success?]]
   [pinkgorilla.nrepl.client.core :refer [connect disconnect send-request! send-request-sync! request-rolling!]] ; side effects
   )
  (:gen-class))

(def cli-options
  ;; see https://github.com/clojure/tools.cli#example-usage
  [["-m" "--mode MODE" "mode: ide or sink"
    :default :help
    :parse-fn #(keyword %)]

   ["-p" "--port PORT" "Port number of the nrepl server."
    :default 9100
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-h" "--host HOST" "host of the nrepl server."
    :default "127.0.0.1"]

   ["-i" "--input FILE" "file to load, default: ./snippets.default.edn"
    :default "./snippets/default.edn"]

   ["-l" "--log-level LOGLEVEL" ":debug :info :warn :error , default: :warn"
    :default :warn
    :parse-fn #(keyword %)]

;
   ])

(defn log-config! [level]
  (timbre/set-config!
   (merge timbre/default-config
          {:min-level ;:info
           [;[#{"pinkgorilla.nrepl.client.connection"} :debug]
          ;[#{"pinkgorilla.nrepl.client.op.eval"} :debug]
            [#{"*"} level]]})))

(defn neval [state msg]
  (println "\r\n" msg)
  (->> msg
       (send-request-sync! state)
       ;print-fragments
       (println "result: ")))

(defn print-forwarded [msg]
  (println (pr-str msg)))

(defn -main [& args]
  (let [config (cli/parse-opts args cli-options)
        options (:options config)]
    (println options)
    (log-config! (:log-level options))
    (case (:mode options)
      :sink
      (do
        (println "printing all sniffing results ... (exit with ctrl+c)")
        (let [conn (connect options)]
          (println (send-request-sync! conn {:op "describe"}))
          (request-rolling! conn {:op "sniffer-sink"} print-forwarded)
          (println (send-request-sync! conn {:op "describe"})))) ; test to see if multiplexer works with open requests.

      :ide
      (let [conn (connect options)
            neval (partial neval conn)
            ops (read-string (slurp (:input options)))]
        (doall (map neval ops)) ; blocking
        ;(send-requests! conn (take 2 ops-ide)) ; 
        ;(read-line)
        (println "quit.")
        (Thread/sleep 1000)
        (disconnect conn))

      :help
      (do (println "To listen (notebook mode): lein client -m sink")
          (println "To eval (ide mode):        lein client -m ide")
          (println "options:")
          (println (:summary config))))))


(comment

  (keyword "g")

  (cli/parse-opts [;"-p" "3008" 
                   "-h" "127.0.0.1"] cli-options)


 ; 
  )