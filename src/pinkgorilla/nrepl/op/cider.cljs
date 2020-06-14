(ns pinkgorilla.nrepl.op.cider
  (:require
   [taoensso.timbre :refer-macros [info warn]]))


(defn send-cider-message!
  [message callback]
  (send-message! :ciders message callback))

(defn get-completions
  "Query the REPL server for autocompletion suggestions. 
   Relies on the cider-nrepl middleware.
   We call the given callback with the list of symbols once the REPL server replies."
  [symbol ns context callback]
  (send-cider-message! {:op "complete" :symbol symbol :ns ns :context context}
                       (fn [msg]
                         (callback (->> (:completions msg)
                                        (map #(:candidate %)))))))

(defn get-completion-doc
  "Queries the REPL server for docs for the given symbol. 
   Relies on the cider-nrepl middleware.
   Calls back with the documentation text"
  [symbol ns callback]
  (send-cider-message! {:op "complete-doc" :symbol symbol :ns ns}
                       (fn [msg]
                         (callback (:completion-doc msg)))))

(defn resolve-symbol
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
  [symbol ns callback]
  (send-cider-message! {:op "info" :symbol symbol :ns ns}
                       (fn [msg]
                         (callback {:symbol (:name msg)
                                    :ns (:ns msg)}))))

(defn stacktrace!
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
  [callback]
  (let [error (atom {})]
  (send-cider-message!
   {:op "stacktrace"}
   (fn [msg] ; CALLBACK THAT PROCESS CIDER MESSAGES
     (let [status (:status msg)]
       (info "err status: " status)
       (if (contains? status :done)
         (callback error)
         (swap! error
                (fn [err ex]
                  (if (:exception err)
                    (assoc-in err [:exception :cause] (:exception ex))
                    (merge ex err)))
                {:exception msg})))))))


(comment
  
  (get-completion-doc 'print-table 'clojure.pprint #(println "docs: " %1))

  
;  
  )