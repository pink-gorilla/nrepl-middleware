(ns pinkgorilla.nrepl.middleware.picasso
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   ;[clojure.tools.reader.edn :as edn]
   [clojure.edn :as edn]
  ; [clojure.tagged-literals]
   [nrepl.transport :as transport]
   [nrepl.middleware :as middleware]
   [nrepl.misc :refer [response-for]]
   [pinkgorilla.nrepl.middleware.formatter :as formatter]
   [pinkgorilla.nrepl.middleware.datafy :refer [datafy-id nav!]]
   [picasso.converter :refer [->picasso]])
  (:import nrepl.transport.Transport))

(set! *default-data-reader-fn* tagged-literal)

;; Stolen from:
;; https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/middleware/pr_values.clj
;; and as a result the structure of this follows that code rather closely

;; This middleware adds :picasso key to the response protocol on the value that results from the evaluation, and
;; then converts the result to edn.


(defn render-value [value]
  (let [r (->picasso value)]
    r))

(defn add-datafy [resp v]
  (if-let [d (datafy-id v)]
    (assoc resp :datafy (pr-str d))
    resp))

(defn add-picasso [resp]
  (if-let [[_ v] (find resp :value)]
  (-> (assoc resp :picasso (formatter/serialize (render-value v)))
      (add-datafy v))
    resp
  ))

(defn convert-response [{:keys [op] :as req} resp]
   ;; we have to transform the rendered value to EDN here, as otherwise
   ;; it will be pr'ed by the print middleware (which comes with the
   ;; eval middleware), meaning that it won't be mapped to EDN when the
   ;; whole message is mapped to EDN later. This has the unfortunate side
   ;; effect that the string will end up double-escaped.
   ;; (assoc resp :value (json/generate-string (render/render v)))
  ;(info "op:" op)
  ;(if (= op "gorilla-nav")
  ;  (let [id (:datafy-id req)
  ;        k (:datafy-k req)
  ;        v (:datafy-v req)
  ;        nav (nav! id k v)]
  ;    (assoc resp :nav (formatter/serialize nav)))
  (if (:as-picasso req)
    (add-picasso resp)
    resp))


(defn transport-picasso-render
  [{:keys [^Transport transport] :as request}]
  (reify Transport
    (recv [this] (.recv transport))
    (recv [this timeout] (.recv transport timeout))
    (send [this resp]
      (.send transport (convert-response request resp))
      this)))

(defn decode [datafy-str]
  (edn/read-string
   {:readers  *data-readers*
    #_{;; 'js (with-meta identity {:punk/literal-tag 'js})
              ;'inst cljs.tagged-literals/read-inst
              ;'uuid cljs.tagged-literals/read-uuid
              ;'queue cljs.tagged-literals/read-queue
       }
    :default tagged-literal}
   datafy-str))

(defn response-nav [request]
  (let [dfy (decode (:datafy request))
        {:keys [idx k v]} dfy
        nav (nav! idx k v)]
    (response-for request
                  :status :done
                  :datafy (pr-str nav))))

(defn wrap-picasso [handler]
  (fn [{:keys [op transport] :as request}]
    (if (= "gorilla-nav" op)
      (nrepl.transport/send transport (response-nav request))
      (handler (assoc request :transport (transport-picasso-render request))))))


;; TODO: No idea whether this still applies to nrepl 0.6
;; nrepl.middleware.print/wrap-print is the new nrepl.middleware.pr-values - see new CHANGELOG.md
;; Unfortunately nREPL's interruptible-eval middleware has a fixed dependency on the pr-values middleware. So here,
;; what we do is fudge the :requires and :expects values to ensure that our rendering middleware gets inserted into
;; the linearized middlware stack between the eval middleware and the pr-values middleware. A bit of a hack!


(middleware/set-descriptor!
 #'wrap-picasso
 {:requires #{#'nrepl.middleware.print/wrap-print}
  :expects  #{"eval"}
  :handles  {"gorilla-nav"  {:doc "datafy/nav"}}})
