(ns pinkgorilla.nrepl.service.add-middleware
  (:require
   [clojure.core.async :refer [<! <!! go]]
   [pinkgorilla.nrepl.client :as client]
   [pinkgorilla.nrepl.handler.nrepl-loader :refer [ops-sniffer]]
   ))

(defn- print-eval-result [fragments]
  (doall
   (map-indexed
    (fn [i f]
      (println i ": " (dissoc f :id #_:session))) fragments))
  fragments)

(def state-a (atom nil))

(defn current-session-id []
  (when-let [state @state-a]
    (when-let [session-id (:session-id @state)]
      session-id)))

(defn status [fragments]
  (:status (last fragments)))

(defn success? [fragments]
  (let [s (status fragments)
        e (filter #(= "error" %) s)]
    (= 0 (count e))))

(defn status-e [fragments]
  (let [s (status fragments)
        e (filter #(= "error" %) s)]
    (if (= 0 (count e))
      (str "Success!")
      (str "Error: " s))))

(defn send! [msg]
  (if-let [send-fn (:send-fn @state-a)]
    (do
      ;(println "sniffer state: " @state-a)
      (send-fn msg))
    (println "send failed - not started")))

 ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  ; (if-let [new-session (:new-session message)]
  ; "interrupt", which will attempt to interrupt the current execution with id provided in the :interrupt-id slot.
   ; "close", which drops the session indicated by the ID in the :session slot. The response message's :status will include :session-closed.
   ; "ls-sessions", which results in a response message containing a list of the IDs of the currently-retained sessions in a :session slot.       



(defn add-middleware!
  [config]
  (let [nrepl-server-config (:nrepl-server config)
        {:keys [bind port]
         :or {bind "127.0.0.1"
              port 9000}}
        nrepl-server-config]
    (println "add-middleware! nrepl port: " port)
    (let [state (client/connect! port)
          exec-sync (fn [op]
                        (client/exec-async! state op)
                          )
          ]
      (reset! state-a {:send-fn (partial client/send! state print-eval-result)
                       :state state})
      (println "connected!")
      (go
        
        (println "init results: "
                 (map  (comp print-eval-result
                             ;status-e 
                             <!! exec-sync) ops-sniffer))
        
        #_(let [r (<! (client/exec-async!
                     state
                     {:op "describe"}))]
          (println "describe result: " (status-e r))
          (print-eval-result r))

        #_(let [r (<! (client/exec-async!
                     state
                     {:op "eval"
                      :code "(require '[pinkgorilla.nrepl.sniffer.middleware])"}))]
          (println "require middleware result: " (status-e r))
          #_(print-eval-result r))

         ;   ;'pinkgorilla.nrepl.middleware/wrap-pinkie
        #_(let [r (<! (client/exec-async!
                     state
                     {:op "add-middleware"
                      :middleware ['pinkgorilla.nrepl.sniffer.middleware/render-values-sniffer]}))]
          (println "add middleware result: " (status-e r))
          #_(print-eval-result r))

         ;(send! {:op "eval" :code "(require '[pinkgorilla.nrepl.sniffer.middleware])"})
         ;(send! {:op "eval" :code "\"pinkgorilla snippet jack-in ..\""})
         ;(send! {:op "eval" :code "(require '[pinkgorilla.ui.hiccup_renderer])"})
         ;(send! {:op "add-middleware" :middleware ['pinkgorilla.nrepl.sniffer.middleware/render-values]})
        (println "snippets connected successfully to nrepl port " port)
        (println "goldly shows them on path /snippets")))))