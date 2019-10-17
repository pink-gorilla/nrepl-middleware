(ns pinkgorilla.middleware.cider
  (:require
   [pinkgorilla.middleware.handle :as mw]
   [pinkgorilla.middleware.sandboxed_interruptible-eval]
   [pinkgorilla.middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
   [cider.nrepl]))


(def ^:private cider-middleware
  "A vector containing the CIDER middleware gorilla repl supports."
  '[cider.nrepl/wrap-complete
    cider.nrepl/wrap-info
    cider.nrepl/wrap-stacktrace])


(def cider-handler (mw/nrepl-handler false cider-middleware))
