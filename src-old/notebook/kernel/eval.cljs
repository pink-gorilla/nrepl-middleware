(ns pinkgorilla.notebook.kernel.eval
  (:require
   [clojure.string :as str]
   [taoensso.timbre :refer [debug info error]]
   [re-frame.core :refer [dispatch]]
   [pinkgorilla.kernel.cljs-helper :refer [send-value]]
   [pinkgorilla.kernel.nrepl-specs] ; bring the specs into scope:
   [pinkgorilla.nrepl.op.cider :refer [stacktrace!]]
   ;PinkGorilla Notebook
   [pinkgorilla.notifications :refer [add-notification notification]]
   [pinkgorilla.util :refer [application-url ws-origin]]))

(defn start! []
  []
  (start-repl! (ws-origin "repl" (application-url))))


#_(defn eval!
    [segment-id code]
    (send-message! :evaluations
                   {:op   "eval"
                    :code code}
                   segment-id))


(defn set-clj-kernel-status [connected session-id]
  (dispatch [:kernel-clj-status-set connected session-id]))

(defn make-cider-stacktrace-request
  "The logic here is a little complicated as cider-nrepl will send the stacktrace information back to
   us in installments. So what we do is we register a handler for cider replies that accumulates the
   information into a single data structure, and when cider-nrepl sends us a done message, indicating
   it has finished sending stacktrace information, we fire an event which will cause the worksheet to
   render the stacktrace data in the appropriate place."
  [err segment-id]
  (let [_ (info "stacktrace-request for err: " err)
        warning? (str/starts-with? err "WARNING")
        error {:error-text err
               :segment-id segment-id}]
    (if warning?
      ; warning -> done
      (dispatch [:evaluator:error-response error])
      ;error -> get stacktrace
      (stacktrace! #(dispatch [:evaluator:error-response
                               (merge error %)])))))

(defn segment-eval-handler [segment-id type data]
  (case type
    :value (send-value segment-id (:data data) (:ns data))
    :console (dispatch [:evaluator:console-response segment-id {:console-response data}])
    :error (make-cider-stacktrace-request data segment-id)
    :done (dispatch [:evaluator:done-response segment-id])))

(defn on-ws-connect-failed [msg]
  (add-notification (notification :danger (str "clj-kernel Fatal Error: " error)))
  (set-clj-kernel-status false nil))

(defn on-ws-session-connect [msg]
  (set-clj-kernel-status true new-session)
  (dispatch [:set-clj-secrets]))

(defn on-ws-session-connect [msg]
  (add-notification (notification :danger (str "clj-kernel error: " error " - trying to recover with session " session-id))))


(defn on-cider []
  cider-cb ; (cider does completions and docstring)
  (do
    (when (s/valid? :nrepl-msg/stacktrace-msg message)
      (info "rcvd valid stacktrace: " message))
    (cider-cb message)
    (when (contains? status :done)
      (swap! ws-repl dissoc [:ciders id]))))



(defn eval!
  "evaluates a notebook segment"
  [state segment-id code]
  (let [handler (partial segment-eval-handler segment-id)
        on-response (partial on-nrepl-eval-response handler eval-id)
        _ (send-message! :callback {:op "eval" :code code} on-response)]))
