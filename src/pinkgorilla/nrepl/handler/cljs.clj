(ns pinkgorilla.nrepl.handler.cljs
  "Experimental CLJS - not quite sure if that makes sense at all")

(def middleware-cljs
  "A vector containing the cljs gorilla repl supports."
  '[cider.piggieback/wrap-cljs-repl])

(defn require-cljs []
  ;; force side effects at runtime in nrepl-process
  (require 'cider.piggieback))