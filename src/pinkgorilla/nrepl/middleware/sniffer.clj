(ns pinkgorilla.nrepl.middleware.sniffer
  (:require
   ;[clojure.tools.logging :refer (info)]
   [nrepl.transport :as transport]
   [nrepl.middleware :as middleware]
   [nrepl.middleware.print]
   [nrepl.misc :refer [response-for] :as misc])
  (:import nrepl.transport.Transport))

(def state (atom {:msg-sink nil
                  :session-id-sink nil}
                 :session-id-source nil))

(defn session-id-
  "extracts the id from a session.
   sometimes sessions are strings, but sometimes 
   they are not."
  [session]
  (when session
    (if (instance? clojure.lang.AReference session)
      (-> session meta :id)
      session)))

(defn status- []
  (dissoc @state :msg-sink))

(defn response-sniff-status
  [msg]
  (response-for msg {:status :done
                     :sniffer-status (status-)}))

(defn response-sniff-source!
  [{:keys [session] :as msg}]
  (let [session (session-id- session)]
    (println "sniffer - setting source session id:" session)
    (swap! state assoc :session-id-source session)
    (println "sniffer - state:" (status-))
    (response-for msg {:status :done
                       :sniffer-status (status-)})))

(defn response-sniff-sink!
  [{:keys [session] :as msg}]
  (let [session (session-id- session)]
    (println "sink msg:" msg)
    (println "sniffer - setting sink session id:" session)
    (swap! state assoc
           :msg-sink msg
           :session-id-sink session)
    (println "sniffer - state:" (status-))
    ; response-msg may NOT contain :status :done
    ; sniffer will forward messages to that request-id
    (response-for msg {; :status :done 
                       :sniffer-status (status-)})))

(defn response-eval-forward
  [msg]
  (let [msg-listener (:msg-sink @state)
        msg-forward (dissoc msg :session :transport
                            :nrepl.middleware.print/print-fn
                            :nrepl.middleware.caught/caught-fn)
        msg (response-for msg-listener {:status :done
                                        :sniffer-forward msg-forward})]
    (println "sniffer forwarding to " (:session-id-sink @state) "message: " msg-forward)
    msg))


; a clean nrepl middleware is found in:
; https://github.com/RickMoynihan/nrebl.middleware/blob/master/src/nrebl/middleware.clj

(defn eval-response [{:keys [code] :as req} {:keys [value] :as resp}]
  (when (and code true); (contains? resp :value))
    (let [msg-listener (:msg-sink @state)
          msg-forward (dissoc resp :session :transport
                              :nrepl.middleware.print/print-fn
                              :nrepl.middleware.caught/caught-fn)
          msg-resp (response-for msg-listener {:sniffer-forward msg-forward})]
      ; printing not allowed here - nrepl would capture this as part of the eval request 
      ;(println "sniffer forwarding response:" msg-resp)
      msg-resp)))

(defn- wrap-sniffer-sender
  "Wraps a `Transport` with code which prints the value of messages sent to
  it using the provided function."
  [{:keys [id op ^Transport transport session] :as request}]
  (reify Transport
    (recv [this]
      (.recv transport))
    (recv [this timeout]
      (.recv transport timeout))
    (send [this resp]
      (.send transport resp)
      (when (and (= (session-id- session) (:session-id-source @state))
                 (:code request))
        (transport/send (:transport (:msg-sink @state))
                        (eval-response request resp)))
      #_(send-to-pinkie! request resp)
      this)))

(defn wrap-sniffer
  [handler]
  (fn [{:keys [^Transport transport op session] :as request}]
    (let [session (session-id- session)]
      (cond
        ; requests handled by sniffer don't have to be processed by other handers
        (= op "sniffer-status")
        (transport/send transport (response-sniff-status request))

        (= op "sniffer-source")
        (transport/send transport (response-sniff-source! request))

        (= op "sniffer-sink")
        (transport/send transport (response-sniff-sink! request))

        :else
        (do (when (and (= op "eval")
                       (= session (:session-id-source @state)))
              (println "sniffer - forwarding eval: " (:code request))
              (if (:msg-sink @state)
                (transport/send (:transport (:msg-sink @state)) (response-eval-forward request))
                (println "sniffer - no sink. cannot forward!"))
                  ;(handler request)
              )
            (handler (assoc request :transport (wrap-sniffer-sender request))))
        ;  (handler request)        
        ))))

(middleware/set-descriptor!
 #'wrap-sniffer
 {:requires #{#'nrepl.middleware.print/wrap-print}
  :expects  #{"eval"}
  :handles {"sniffer-status"
            {:doc "status of sniffer middleware"}

            "sniffer-source"
            {:doc "start sniffing current session"}

            "sniffer-sink"
            {:doc "called from notebook. destination for forwarded events "}}})



; {:doc "Provides sniffer status"
;             :requires {"prefix" "The prefix to complete."}
;             :optional {"ns" "The namespace in which we want to obtain completion candidates. Defaults to `*ns*`."
;                        "complete-fn" "The fully qualified name of a completion function to use instead of the default one (e.g. `my.ns/completion`)."
;                        "options" "A map of options supported by the completion function."}
;             :returns {"completions" "A list of completion candidates. Each candidate is a map with `:candidate` and `:type` keys. Vars also have a `:ns` key."}}}})

