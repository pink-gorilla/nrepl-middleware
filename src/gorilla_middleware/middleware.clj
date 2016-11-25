(ns gorilla-middleware.middleware
  (:require [clojure.tools.nrepl.server :as srv]
            [gorilla-middleware.sandboxed_interruptible-eval]
            [gorilla-middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            [clojure.tools.nrepl :as nrepl]
            [cider.nrepl :as cider]))

;; Dirty hack to swap nREPL interruptible-eval
(defn- middlewares
  [middlewares sandbox]
  (if sandbox
    (->> middlewares
         (map #(if (= (-> % meta :name name) "interruptible-eval")
                 #'gorilla-middleware.sandboxed_interruptible-eval/interruptible-eval
                 %))
         (into []))
    middlewares))

(defn nrepl-handler
  [sandbox cid-mw-vars]
  (let [cider-mw (map resolve cid-mw-vars)
        middleware (conj cider-mw #'render-mw/render-values)]
    (with-redefs [srv/default-middlewares (middlewares srv/default-middlewares sandbox)]
      (apply srv/default-handler middleware))))


(def ^:private cider-middleware
  "A vector containing the CIDER middleware gorilla repl supports."
  '[cider.nrepl.middleware.complete/wrap-complete
    cider.nrepl.middleware.info/wrap-info
    cider.nrepl.middleware.stacktrace/wrap-stacktrace])


(def gorilla-handler (atom (nrepl-handler false cider-middleware)))
