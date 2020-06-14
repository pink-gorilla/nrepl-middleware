(ns pinkgorilla.nrepl.op.cider
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [pinkgorilla.nrepl.ws.client :refer [make-request!]]))


(defn get-completions
  "Query the REPL server for autocompletion suggestions. 
   Relies on the cider-nrepl middleware.
   We call the given callback with the list of symbols once the REPL server replies."
  [state symbol ns context]
  (make-request!
   state
   {:op "complete" :symbol symbol :ns ns :context context}
   :transform-fn
   (fn [fragments]
     (map (:completions msg) fragments) ;  #(:candidate %)
     )))

(defn get-completion-doc
  "Queries the REPL server for docs for the given symbol. 
   Relies on the cider-nrepl middleware.
   Calls back with the documentation text"
  [state symbol ns callback]
  (make-request!
   state
   {:op "complete-doc" :symbol symbol :ns ns}
   :transform-fn
   (fn [fragments]
     (map :completion-doc fragments))))

(defn resolve-symbol
  "resolve a symbol to get its namespace takes the symbol and the namespace 
   that should be used as context.
   Relies on the cider-nrepl middleware. 
   Returns:
   - the symbol and the symbol's namespace"
  [state symbol ns]
  (make-request!
   state
   {:op "info" :symbol symbol :ns ns}
   :transform-fn
   (fn [fragments]
     (map #({:symbol (:name %)
             :ns (:ns &)}) fragments))))

(defn stacktrace!
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
  [state]
  (make-request!
   state
   {:op "stacktrace"}
   :transform-fn
   (fn [fragments]
     (map :exception fragments))))


(comment

  (get-completion-doc 'print-table 'clojure.pprint #(println "docs: " %1))


;  
  )