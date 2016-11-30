(ns gorilla-middleware.cider
  (:require [gorilla-middleware.handle :as mw]
            [gorilla-middleware.sandboxed_interruptible-eval]
            [gorilla-middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            [cider.nrepl :as cider]))


(def ^:private cider-middleware
  "A vector containing the CIDER middleware gorilla repl supports."
  '[cider.nrepl.middleware.complete/wrap-complete
    cider.nrepl.middleware.info/wrap-info
    cider.nrepl.middleware.stacktrace/wrap-stacktrace])


(def cider-handler (mw/nrepl-handler false cider-middleware))
