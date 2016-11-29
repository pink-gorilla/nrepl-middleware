(ns gorilla-middleware.cljs
  "Experimental CLJS"
  (:require [gorilla-middleware.handle :as mw]
            [gorilla-middleware.sandboxed_interruptible-eval]
            [gorilla-middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            [clojure.tools.nrepl.server :as server]
            [cemerick.piggieback :as pb]))

(def ^:private cljs-middleware
  "A vector containing the cljs gorilla repl supports."
  '[cemerick.piggieback/wrap-cljs-repl])


(def cljs-handler (atom (mw/nrepl-handler false cljs-middleware)))