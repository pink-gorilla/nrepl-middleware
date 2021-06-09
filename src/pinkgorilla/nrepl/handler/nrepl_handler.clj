(ns pinkgorilla.nrepl.handler.nrepl-handler
  (:require
   [nrepl.server :as nrepl-server]
     ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution). 
   [pinkgorilla.nrepl.handler.cider :refer [require-cider middleware-cider middleware-cider-resolve]]
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
      (with-redefs [nrepl-server/default-middlewares (swap-interruptible-eval nrepl-server/default-middlewares)]
        (apply nrepl-server/default-handler mw))
      (apply nrepl-server/default-handler mw)
      ;(apply nrepl-server/default-handler)
      )))

(defn make-default-handler []
  (require-cider)
  (require-gorilla)
  (resolve 'cider.nrepl/wrap-info)
  ;(let [middleware-cider-resolved (middleware-cider-resolve)]
  (make-nrepl-handler
   (into []
         (concat middleware-cider middleware-gorilla))
   false))
