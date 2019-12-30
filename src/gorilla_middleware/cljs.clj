(ns gorilla-middleware.cljs
  "Experimental CLJS"
  (:require [pinkgorilla.middleware.handle :as mw]
            [pinkgorilla.middleware.sandboxed_interruptible-eval]
            [pinkgorilla.middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            ;; [nrepl.server :as server]
            ;; [cider.piggieback :as pb]
            ))

(def ^:private cljs-middleware
  "A vector containing the cljs gorilla repl supports."
  '[cider.piggieback/wrap-cljs-repl])

(def cljs-handler (mw/nrepl-handler false cljs-middleware))
