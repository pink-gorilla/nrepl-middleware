(ns pinkgorilla.nrepl.handler.nrepl-handler
  (:require
   [nrepl.server :as srv]
     ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution). 
   [pinkgorilla.nrepl.handler.cider :refer [require-cider middleware-cider]]
   [pinkgorilla.nrepl.handler.gorilla :refer [require-gorilla middleware-gorilla]]))

;; Dirty hack to swap nREPL interruptible-eval
(defn- swap-interruptible-eval
  [middlewares]
  (require 'pinkgorilla.nrepl.middleware.sandboxed_interruptible-eval)
  (let [ie (resolve 'pinkgorilla.nrepl.middleware.sandboxed_interruptible-eval/interruptible-eval)]
    (->> middlewares
         (map #(if (= (-> % meta :name name) "interruptible-eval")
                 ie
                 %))
         (into []))
    middlewares))

(defn- make-nrepl-handler
  [mw-vars sandbox]
  (let [mw (map resolve mw-vars)]
    (if sandbox
      (with-redefs [srv/default-middlewares (swap-interruptible-eval srv/default-middlewares)]
        (apply srv/default-handler mw))
      (apply srv/default-handler mw))))


(defn make-default-handler []
  (require-cider)
  (require-gorilla)
  (make-nrepl-handler
   (into []
         (concat middleware-cider middleware-gorilla))
   false))

;  (nrepl-handler false cider-middleware))