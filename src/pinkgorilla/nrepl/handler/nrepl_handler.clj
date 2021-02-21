(ns pinkgorilla.nrepl.handler.nrepl-handler
  (:require
   [nrepl.server :as srv]
   [pinkgorilla.nrepl.middleware.sandboxed_interruptible-eval]
   [pinkgorilla.nrepl.middleware.picasso :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
   [nrepl.middleware.print] ;; side-effects ?
   [picasso.default-config] ; side-effects
   [pinkgorilla.notebook.repl] ; side-effects
   ))

;; Dirty hack to swap nREPL interruptible-eval
(defn- middlewares
  [middlewares sandbox]
  (if sandbox
    (->> middlewares
         (map #(if (= (-> % meta :name name) "interruptible-eval")
                 #'pinkgorilla.nrepl.middleware.sandboxed_interruptible-eval/interruptible-eval
                 %))
         (into []))
    middlewares))

(defn nrepl-handler
  [sandbox mw-vars]
  (let [mw (map resolve mw-vars)
        middleware (conj mw #'render-mw/wrap-picasso)]
    (with-redefs [srv/default-middlewares (middlewares srv/default-middlewares sandbox)]
      (apply srv/default-handler middleware))))
