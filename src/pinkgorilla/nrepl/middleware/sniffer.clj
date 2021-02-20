(ns pinkgorilla.nrepl.middleware.sniffer
  (:require
   ;[clojure.tools.logging :refer (info)]
   [clojure.core.async :refer [chan >! go]]
   [nrepl.transport :as transport]
   [nrepl.middleware :as middleware]
   [nrepl.middleware.print]
   [nrepl.misc :refer [response-for] :as misc]
   [pinkgorilla.nrepl.middleware.formatter :as formatter]
   [pinkgorilla.nrepl.ignore :refer [ignore?]]
   [pinkgorilla.nrepl.logger :as logger]
   [pinkgorilla.nrepl.sniffer.notebook :refer [->notebook]])
  (:import nrepl.transport.Transport))

(def state (atom {:msg-listen nil
                  :session-id-sniffing nil
                  :session-id-listen nil}))

(defn session-id [session]
  (when session
    (if (instance? clojure.lang.AReference session)
      (-> session meta :id)
      session)))

(defn status []
  (dissoc @state :msg-listen))

(defn response-sniff-status
  [msg]
  (response-for msg {:status :done
                     :sniffer-status (status)}))

(defn response-sniff-on!
  [{:keys [code session] :as msg}]
  (let [session (session-id session)]
    (println "starting sniffing on session id:" session)
    (swap! state assoc :session-id-sniffing session)
    (response-for msg {:status :done
                       :sniffer-status (status)})))

(defn response-sniff-listen!
  [{:keys [session ns] :as msg}]
  (let [session (session-id session)]
    (println "session id:" session)
    (swap! state assoc
           :msg-listen msg
           :session-id-listen session)
    (println "new sniff state:" (status))
    (response-for msg {; :status :done ; will send forwarded messages here
                       :sniffer-status (status)})))

(defn response-eval-forward
  [msg]
  (let [msg-listener (:msg-listener @state)
        msg-forward (dissoc msg :session :transport
                            :nrepl.middleware.print/print-fn
                            :nrepl.middleware.caught/caught-fn)
        msg (response-for msg-listener {:status :done
                                        :sniffer-forward msg-forward}) 
        ]
    (println "forwarding to " (session-id (:session msg)) "message: " msg-forward)
    msg
    ))


; a clean nrepl middleware is found in:
; https://github.com/RickMoynihan/nrebl.middleware/blob/master/src/nrebl/middleware.clj

;; Stolen from:
;; https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/middleware/pr_values.clj
;; and as a result the structure of this follows that code rather closely

;; This middleware function calls the gorilla-repl render protocol on the value that 
;; results from the evaluation, and then converts the result to edn.

(def chan-eval-results (chan))

(defn convert-response [msg resp]
   ;; we have to transform the rendered value to EDN here, as otherwise
   ;; it will be pr'ed by the print middleware (which comes with the
   ;; eval middleware), meaning that it won't be mapped to EDN when the
   ;; whole message is mapped to EDN later. This has the unfortunate side
   ;; effect that the string will end up double-escaped.
   ;; (assoc resp :value (json/generate-string (render/render v)))
  (if (ignore? msg resp)
    resp
    (do
      (logger/on-nrepl-eval msg resp)
      (if-let [nb (->notebook msg resp)]
        (do
          (go (>! chan-eval-results nb))
          ;(publish-eval! eval-result)
          #_(client/send!  {:op "eval"
                            :code (str "(systems.snippets/publish-eval! "
                                       (pr-str eval-result)
                                       ")")})
          (assoc resp :pinkie (formatter/serialize (:pinkie nb)))) ; this is used by the notebook
        resp))))



(defn render-values-sniffer
  [handler]
  (fn [{:keys [^Transport transport op session] :as msg}]
    (let [session (session-id session)]
      (cond
        (= op "sniffer-status-get")
        (transport/send transport (response-sniff-status msg))

        (= op "sniffer-on")
        (transport/send transport (response-sniff-on! msg))

        (= op "sniffer-listen")
        (transport/send transport (response-sniff-listen! msg))

        (and (= op "eval") (= session (:session-id-sniffing @state)))
        (do (println "forwarding eval: " (:code msg))
            (if (:msg-listen @state)
              (transport/send (:transport (:msg-listen @state)) (response-eval-forward msg))
              (println "no listener. cannot forward!")
              )
            (handler msg))

        :else
  ;  (handler msg)        
        (handler
         (assoc msg :transport
                (reify Transport
                  (recv [this]
                    (println "rcvd!")
                    (.recv transport))
                  (recv [this timeout]
                    (.recv transport timeout))
                  (send [this resp]
                    (.send transport (convert-response msg resp))
                    this))))))))


;; TODO: No idea whether this still applies to nrepl 0.6
;; nrepl.middleware.print/wrap-print is the new nrepl.middleware.pr-values - see new CHANGELOG.md
;; Unfortunately nREPL's interruptible-eval middleware has a fixed dependency on the pr-values middleware. So here,
;; what we do is fudge the :requires and :expects values to ensure that our rendering middleware gets inserted into
;; the linearized middlware stack between the eval middleware and the pr-values middleware. A bit of a hack!


(middleware/set-descriptor!
 #'render-values-sniffer
 {:requires #{#'nrepl.middleware.print/wrap-print}
  :expects  #{"eval"}
  :handles {"sniffer-on"
            {}

            "sniffer-status-get"
            {:doc "Provides sniffer status"
             :requires {"prefix" "The prefix to complete."}
             :optional {"ns" "The namespace in which we want to obtain completion candidates. Defaults to `*ns*`."
                        "complete-fn" "The fully qualified name of a completion function to use instead of the default one (e.g. `my.ns/completion`)."
                        "options" "A map of options supported by the completion function."}
             :returns {"completions" "A list of completion candidates. Each candidate is a map with `:candidate` and `:type` keys. Vars also have a `:ns` key."}}}})

#_(defn send-to-pinkie! [{:keys [code] :as req} {:keys [value] :as resp}]
    (when (and code true); (contains? resp :value))
      (println "evalpinkie:" (read-string code) value))
    resp)

#_(defn- wrap-pinkie-sender
    "Wraps a `Transport` with code which prints the value of messages sent to
  it using the provided function."
    [{:keys [id op ^Transport transport] :as request}]
    (reify transport/Transport
      (recv [this]
        (.recv transport))
      (recv [this timeout]
        (.recv transport timeout))
      (send [this resp]
        (.send transport
               (send-to-pinkie! request resp))
        this)))

#_(defn wrap-pinkie [handler]
    (fn [{:keys [id op transport] :as request}]
      (if (= op "evalpinkie")
      ;(rebl/ui)
        (handler (assoc request :transport (wrap-pinkie-sender request))))))

#_(middleware/set-descriptor! #'wrap-pinkie
                              {:requires #{#'nrepl.middleware.print/wrap-print}
                               :expects  #{"eval"}
                               :handles {"evalpinkie" "eval with pinkie conversion"}})