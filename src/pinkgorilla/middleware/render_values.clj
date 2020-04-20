(ns pinkgorilla.middleware.render-values
  (:require
   ;[clojure.tools.logging :refer (info)]
   [nrepl.transport :as transport]
   [nrepl.middleware.print]
   [nrepl.middleware :as middleware]
   [pinkgorilla.middleware.formatter :as formatter]
   [pinkgorilla.ui.gorilla-renderable :refer [#_render render-renderable-meta]])
  (:import nrepl.transport.Transport))


;; TODO: This might no longer be true as of nrepl 0.6

;; Stolen from:
;; https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/middleware/pr_values.clj
;; and as a result the structure of this follows that code rather closely

;; This middleware function calls the gorilla-repl render protocol on the value that results from the evaluation, and
;; then converts the result to edn.


(defn render-value [value]
  (let [r (render-renderable-meta value)]
    r))

(defn convert-response [msg resp]
   ;; we have to transform the rendered value to EDN here, as otherwise
   ;; it will be pr'ed by the print middleware (which comes with the
   ;; eval middleware), meaning that it won't be mapped to EDN when the
   ;; whole message is mapped to EDN later. This has the unfortunate side
   ;; effect that the string will end up double-escaped.
   ;; (assoc resp :value (json/generate-string (render/render v)))
  (if-let [[_ v] (and (:as-html msg) (find resp :value))]
    (assoc resp :value (formatter/serialize (render-value v)))
    resp))

(defn render-values
  [handler]
  (fn [{:keys [^Transport transport] :as msg}]
    (handler (assoc msg :transport (reify Transport
                                     (recv [this] (.recv transport))
                                     (recv [this timeout] (.recv transport timeout))
                                     (send [this resp]
                                       (.send transport (convert-response msg resp))
                                       this))))))


;; TODO: No idea whether this still applies to nrepl 0.6
;; nrepl.middleware.print/wrap-print is the new nrepl.middleware.pr-values - see new CHANGELOG.md
;; Unfortunately nREPL's interruptible-eval middleware has a fixed dependency on the pr-values middleware. So here,
;; what we do is fudge the :requires and :expects values to ensure that our rendering middleware gets inserted into
;; the linearized middlware stack between the eval middleware and the pr-values middleware. A bit of a hack!


(middleware/set-descriptor! #'render-values
                            {:requires #{#'nrepl.middleware.print/wrap-print}
                             :expects  #{"eval"}
                             :handles  {}})
