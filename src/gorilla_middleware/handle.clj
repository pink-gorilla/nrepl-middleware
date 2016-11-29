(ns gorilla-middleware.handle
  (:require [clojure.tools.nrepl.server :as srv]
            [gorilla-middleware.sandboxed_interruptible-eval]
            [gorilla-middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            [clojure.tools.nrepl :as nrepl]))

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
  [sandbox mw-vars]
  (let [mw (map resolve mw-vars)
        middleware (conj mw #'render-mw/render-values)]
    (with-redefs [srv/default-middlewares (middlewares srv/default-middlewares sandbox)]
      (apply srv/default-handler middleware))))
