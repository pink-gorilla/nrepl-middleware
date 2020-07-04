(ns pinkgorilla.nrepl.middleware.render-values
  (:require
   ;[clojure.tools.logging :refer (info)]
   [nrepl.transport :as transport]
   [nrepl.middleware.print]
   [nrepl.middleware :as middleware]
   [pinkgorilla.nrepl.middleware.formatter :as formatter]
   [picasso.converter :refer [->picasso]]
   [pinkgorilla.notebook.repl] ; side-effects
   [pinkgorilla.nrepl.middleware.datafy :refer [datafy-id nav!]])
  (:import nrepl.transport.Transport))


;; TODO: This might no longer be true as of nrepl 0.6

;; Stolen from:
;; https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/middleware/pr_values.clj
;; and as a result the structure of this follows that code rather closely

;; This middleware function calls the gorilla-repl render protocol on the value that results from the evaluation, and
;; then converts the result to edn.


(defn render-value [value]
  (let [r (->picasso value)]
    r))

(defn add-datafy [resp v]
  (if-let [d (datafy-id v)]
    (assoc resp :datafy-id d)
    resp))

(defn convert-response [{:keys [op] :as msg} resp]
   ;; we have to transform the rendered value to EDN here, as otherwise
   ;; it will be pr'ed by the print middleware (which comes with the
   ;; eval middleware), meaning that it won't be mapped to EDN when the
   ;; whole message is mapped to EDN later. This has the unfortunate side
   ;; effect that the string will end up double-escaped.
   ;; (assoc resp :value (json/generate-string (render/render v)))
  (if (= op "gorilla-nav")
    (let [id (:datafy-id msg)
          k (:datafy-k msg)
          v (:datafy-v msg)
          nav (nav! id k v)]
      (assoc resp :nav (formatter/serialize nav)))
    (if-let [[_ v] (and (:as-picasso msg) (find resp :value))]
      (-> (assoc resp :picasso (formatter/serialize (render-value v)))
          (add-datafy v))
      resp)))

(defn render-values-req
  [{:keys [^Transport transport] :as request}]
  (reify Transport
    (recv [this] (.recv transport))
    (recv [this timeout] (.recv transport timeout))
    (send [this resp]
      (.send transport (convert-response request resp))
      this)))

(defn render-values [handler]
  (fn [{:keys [op] :as request}]
    (handler (assoc request :transport (render-values-req request)))))


;; TODO: No idea whether this still applies to nrepl 0.6
;; nrepl.middleware.print/wrap-print is the new nrepl.middleware.pr-values - see new CHANGELOG.md
;; Unfortunately nREPL's interruptible-eval middleware has a fixed dependency on the pr-values middleware. So here,
;; what we do is fudge the :requires and :expects values to ensure that our rendering middleware gets inserted into
;; the linearized middlware stack between the eval middleware and the pr-values middleware. A bit of a hack!


(middleware/set-descriptor! #'render-values
                            {:requires #{#'nrepl.middleware.print/wrap-print}
                             :expects  #{"eval"}
                             :handles  {"gorilla-nav" "datafy nav"}})
