(ns pinkgorilla.nrepl.middleware.sniffer
  " observes a ide nrepl connection
    sends evals to a sink (typiccally pinkgorilla notebook)
   "
  (:require
  ; [clojure.set]
   [taoensso.timbre :as timbre :refer [debug info infof warn error]]
   [nrepl.transport :as transport]
   [nrepl.middleware :as middleware]
   [nrepl.middleware.print]
   [nrepl.misc :refer [response-for] :as misc]
   [pinkgorilla.nrepl.logger :refer [log]]
   [pinkgorilla.nrepl.middleware.picasso :refer [add-picasso]])
  (:import nrepl.transport.Transport))

(defonce state (atom {:msg-sink nil
                      :session-id-sink nil
                      :session-id-source nil}))

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
  [req]
  (response-for req {:status :done
                     :sniffer-status (status-)}))

(defn sniff-on [{:keys [session] :as req}]
  (let [session (session-id- session)]
    ;(info "sniffer - setting source session id:" session)
    (swap! state assoc :session-id-source session)))

(defn sniff-off []
  (swap! state assoc :session-id-source nil))

(defn response-sniff-source!
  [req]
  (sniff-on req)
  (info "sniffer state:" (status-))
  (response-for req {:status :done
                     :sniffer-status (status-)}))

(defn response-sniff-sink!
  "registers a sink, so sniffer knows to which nrepl-session
   to send the sniffed evals to"
  [{:keys [session] :as req}]
  (let [session (session-id- session)]
    ;(debug "sink msg:" (dissoc req :session))
    (infof "sniffer sink: %s" session)
    (swap! state assoc
           :msg-sink req
           :session-id-sink session)
    (info "sniffer state:" (status-))
    ; response-msg may NOT contain :status :done
    ; sniffer will forward messages to that request-id
    (response-for req {; :status :done 
                       :sniffer-status (status-)})))

(defn res-eval-forward
  "forwards an nrepl message to the sink."
  [req]
  (let [req-listener (:msg-sink @state)
        req-forward (select-keys req [:id :op :code])
        req-send (response-for req-listener {:sniffer-forward req-forward})]
    ;(infof "sniffer forward to: %s msg: %s" (:session-id-sink @state) req-forward)
    ;(when (:code req)
    ;  (info "code meta data: " (meta (:code req))))
    req-send))

(defn eval-res [{:keys [code] :as req} {:keys [value] :as res}]
  (when (and code true); (contains? res :value))
    (let [msg-listener (:msg-sink @state)
          res (if (:as-picasso res)
                res
                (add-picasso res))
          res-forward (select-keys res [:id :status :ns :picasso :out :err])
          res-resp (response-for msg-listener {:sniffer-forward res-forward})]
      ; printing not allowed here - nrepl would capture this as part of the eval request 
      res-resp)))

; a clean nrepl middleware is found in:
; https://github.com/RickMoynihan/nrebl.middleware/blob/master/src/nrebl/middleware.clj


(defn commands [req res]
  (let [v (:value res)]
    (when v
      (case v
        :gorilla/on (do (log ":gorilla/on - enabling sniffing.")
                        (sniff-on res))
        :gorilla/off (do (log ":gorilla/off - disabling sniffing.")
                         (sniff-off))
        nil))))

(defn- wrap-sniffer-sender
  "Wraps a `Transport` with code which prints the value of messages sent to
  it using the provided function."
  [{:keys [id op ^Transport transport session] :as req}]
  (reify Transport
    (recv [this]
      (.recv transport))
    (recv [this timeout]
      (.recv transport timeout))
    (send [this res]
      (.send transport res)
      (commands req res)
      (when (and (= (session-id- session) (:session-id-source @state))
                 (:code req))
        (let [forward-res  (eval-res req res)]
          (if (:msg-sink @state)
            (do (log (str "sniffer forwarding res: " forward-res))
                (transport/send (:transport (:msg-sink @state)) forward-res))
            (log (str "no-sink - not forwarding res: " forward-res)))))
      this)))

(defn wrap-sniffer
  [handler]
  (fn [{:keys [^Transport transport op code session] :as req}]
    (let [session (session-id- session)]
      (cond
        ; requests handled by sniffer don't have to be processed by other handers
        (= op "sniffer-status")
        (transport/send transport (response-sniff-status req))

        (= op "sniffer-source")
        (transport/send transport (response-sniff-source! req))

        (= op "sniffer-sink")
        (transport/send transport (response-sniff-sink! req))

        :else
        (do (when (and (= op "eval")
                       (not (= code ":gorilla/off"))
                       (= session (:session-id-source @state)))
              ;(info "sniffer - forwarding eval: " (:code req)) ; res-eval-forward does the logging
              (if (:msg-sink @state)
                (do (info "forwarding req: " code)
                    (transport/send (:transport (:msg-sink @state)) (res-eval-forward req)))
                (warn "sniffer - no sink. cannot forward code: " code)) ; "state: " @state
                  ;(handler request)
              )
            (handler (assoc req :transport (wrap-sniffer-sender req))))
        ;  (handler request)        
        ))))


; https://nrepl.org/nrepl/design/middleware.html


(middleware/set-descriptor!
 #'wrap-sniffer
 {;:requires #{}
  :requires #{#'nrepl.middleware.print/wrap-print}
  :expects  ; expects get executed before this middleware
  #{"eval"
    #'pinkgorilla.nrepl.middleware.picasso/wrap-picasso}
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

