(ns pinkgorilla.nrepl.server.add-middleware
  (:require
   [pinkgorilla.nrepl.client :as client]
   [pinkgorilla.nrepl.helper :refer [print-fragments status success?]]
   [pinkgorilla.nrepl.handler.nrepl-loader :refer [ops-sniffer]]))

(defn status-e [fragments]
  (let [s (status fragments)
        e (filter #(= "error" %) s)]
    (if (= 0 (count e))
      (str "Success!")
      (str "Error: " s))))

(defn add-middleware!
  [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
        nrepl-server-config]
    (println "add-middleware! nrepl port: " port)
    (let [state (client/connect! port)
          request! (partial client/request! state)]
      (println "connected!")
      (println "init results: "
               (map  (comp print-fragments
                             ;status-e 
                           request!) ops-sniffer)))))