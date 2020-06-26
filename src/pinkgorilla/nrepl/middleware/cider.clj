(ns pinkgorilla.nrepl.middleware.cider
  (:require
   [pinkgorilla.nrepl.middleware.handle :refer [nrepl-handler]]
    ;; [cider.nrepl]
   ))

(def ; ^:private 
  cider-middleware
  "A vector containing the CIDER middleware pinkgorilla supports."
  '[cider.nrepl/wrap-complete
    cider.nrepl/wrap-info
    cider.nrepl/wrap-stacktrace])

(defn cider-handler []
  ;; force side effects at runtime
  (require 'cider.nrepl)
  (nrepl-handler false cider-middleware))
