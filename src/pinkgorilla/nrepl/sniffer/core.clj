(ns pinkgorilla.nrepl.sniffer.core
  (:require
   [clojure.core.async :refer [<! go]]
   [pinkgorilla.nrepl.client :as client]
   ;[pinkgorilla.nrepl.sniffer.middleware]
   ))

(defn port-from-file []
  (try
    (Integer/parseInt (slurp ".nrepl-port"))
    (catch clojure.lang.ExceptionInfo e 0)
    (catch Exception e 0)))

(defn- print-eval-result [fragments]
  (doall
   (map-indexed
    (fn [i f]
      (println i ": " (dissoc f :id #_:session))) fragments)))

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

(defn start-sniffer!
  ([]
   (start-sniffer! (port-from-file)))
  ([port]
   (println "start-sniffer! nrepl port: " port)
   (let [state (client/connect! port)]
     (reset! state-a {:send-fn (partial client/send! state print-eval-result)
                      :state state})
     (println "connected!")
     (go
       (let [r (<! (client/exec-async!
                    state
                    {:op "describe"}))]
         (println "describe result: " (status-e r))
         (print-eval-result r))

       (let [r (<! (client/exec-async!
                    state
                    {:op "eval"
                     :code "(require '[pinkgorilla.nrepl.sniffer.middleware])"}))]
         (println "require middleware result: " (status-e r))
         #_(print-eval-result r))

         ;   ;'pinkgorilla.nrepl.middleware/wrap-pinkie
       (let [r (<! (client/exec-async!
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


; demo see: profiles/sniffer/app or run "lein sniffer"

  ; "clone", which will cause a new session to be retained. 
  ; The ID of this new session will be returned in a response message 
  ; in a :new-session slot. The new session's state (dynamic scope, etc)
  ;  will be a copy of the state of the session identified in 
  ;  the :session slot of the request.
  ; (if-let [new-session (:new-session message)]
  ; "interrupt", which will attempt to interrupt the current execution with id provided in the :interrupt-id slot.
   ; "close", which drops the session indicated by the ID in the :session slot. The response message's :status will include :session-closed.
   ; "ls-sessions", which results in a response message containing a list of the IDs of the currently-retained sessions in a :session slot.       


(def xxx
  {:aux {:cider-version {:incremental 2, :major 0, :minor 25, :qualifier [], :version-string "0.25.2"}
         :current-ns "shadow.user"}
   :ops {:init-debugger {}
         :stdin {}
         :refresh-clear {}
         :inspect-get-path {}
         :debug-instrumented-defs {}
         :classpath {}
         :spec-example {}
         :format-edn {}
         :debug-middleware {}
         :ns-path {}
         :inspect-push {}
         :inspect-next-page {}
         :ns-aliases {}
         :refresh {}
         :retest {}
         :fn-refs {}
         :test-all {}
         :ns-list-vars-by-name {}
         :resources-list {}
         :eldoc-datomic-query {}
         :undef {}
         :inspect-refresh {}
         :close {}
         :complete-doc {}
         :ns-list {}
         :inspect-def-current-value {}
         :set-max-samples {}
         :fn-deps {}
         :ns-load-all {}
         :debug-input {}
         :format-code {}
         :load-file {}
         :test-var-query {}
         :ns-vars {}
         :clojuredocs-refresh-cache {}
         :ls-sessions {}
         :eldoc {}
         :resource {}
         :clone {}
         :is-var-profiled {}
         :out-unsubscribe {}
         :inspect-prev-page {}
         :cider-version {}
         :toggle-profile-ns {}
         :toggle-profile {}
         :profile-summary {}
         :describe {}
         :info {}
         :interrupt {}
         :stacktrace {}
         :ns-vars-with-meta {}
         :apropos {}
         :complete {}
         :inspect-set-page-size {}
         :macroexpand {}
         :spec-list {}
         :toggle-trace-ns {}
         :track-state-middleware {}
         :toggle-trace-var {}
         :slurp {}
         :get-max-samples {}
         :spec-form {}
         :profile-var-summary {}
         :clojuredocs-lookup {}
         :clear-profile {}
         :out-subscribe {}
         :inspect-pop {}
         :inspect-clear {}
         :complete-flush-caches {}
         :test {}
         :refresh-all {}
         :test-stacktrace {}
         :content-type-middleware {}
         :eval {}}
   :session "85555b99-168c-42e7-8ac2-0eac1abff7c2"
   :status ["done"]
   :versions {:clojure {:incremental 1, :major 1, :minor 10, :version-string "1.10.1"}
              :java {:incremental 6, :major 11, :minor 0, :version-string "11.0.6"}
              :nrepl {:incremental 0, :major 0, :minor 8, :version-string "0.8.0-alpha5"}}})


