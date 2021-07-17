(ns pinkgorilla.nrepl.kernel.kernel
  (:require
   [clojure.core.async :refer [<! >! chan close! go]]
   [taoensso.timbre :as timbre :refer [debug debugf info error]]
   [picasso.id :refer [guuid]]
   [picasso.kernel.protocol :refer [kernel-eval]]
   [pinkgorilla.nrepl.client.core :refer [send-request! op-eval-picasso op-stacktrace]]
   [pinkgorilla.nrepl.kernel.connection :refer [nrepl-conn]]))

(defn stacktrace? [eval-result]
  (when (:err eval-result)
    (when (first (:err eval-result))
      true)))

(defn get-stacktrace [conn eval-result]
  (go (let [_ (info "getting stacktrace of exception.")
            st (<! (send-request! conn (op-stacktrace)))]
        (merge eval-result st))))

(defmethod kernel-eval :clj [{:keys [id code]
                              :or {id (guuid)}}]
  (let [c (chan)]
    (debug "clj-eval: " code)
    (go (try (let [;_ (info "nrepl: " @nrepl)
                   conn (:conn @nrepl-conn)
                   eval-result (<! (send-request! conn (op-eval-picasso code)))
                   _ (info "nrepl eval result: " eval-result)
                   ;eval-result (if (stacktrace? eval-result)
                   ;              (get-stacktrace conn eval-result)
                   ;              eval-result)
                   ]
               (>! c (merge eval-result {:id id})))
             (catch #?(:cljs js/Error :clj Exception) e
               (error "nrepl eval ex: " e)
               (>! c {:id id
                      :error e})))
        (close! c))
    c))

; todo: fragments
; seg-id (keyword id)
; segment (get-in notebook [:segments seg-id])
; result-new (process-fragment segment msg)