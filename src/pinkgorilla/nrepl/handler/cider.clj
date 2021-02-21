(ns pinkgorilla.nrepl.handler.cider)

(def middleware-cider
  "A vector containing the CIDER middleware pinkgorilla supports."
  '[cider.nrepl/wrap-complete
    cider.nrepl/wrap-info
    cider.nrepl/wrap-stacktrace])

(defn require-cider []
  ;; force side effects at runtime in nrepl-process
  (require 'cider.nrepl))


