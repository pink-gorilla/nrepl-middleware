(ns pinkgorilla.nrepl.op.cider
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))

(defn completions
  "Query the REPL server for autocompletion suggestions. 
   Relies on the cider-nrepl middleware."
  [conn symbol ns context]
  (nrepl-op-complete
   conn
   {:op "complete" :symbol symbol :ns ns :context context}
   (fn [fragments]
     ;(map :completions fragments) ;  #(:candidate %)
     (into []
           (apply conj (map :completions fragments))))))

(defn doc-string
  "Queries the REPL server for docs for the given symbol. 
   Relies on the cider-nrepl middleware."
  [conn symbol ns]
  (nrepl-op-complete
   conn
   {:op "complete-doc" :symbol symbol :ns ns}
   (fn [fragments]
     (map :completion-doc fragments))))

(defn resolve-symbol
  "resolve a symbol to get its namespace takes the symbol and the namespace 
   that should be used as context.
   Relies on the cider-nrepl middleware. 
   Returns:
   - the symbol and the symbol's namespace"
  [conn symbol ns]
  (nrepl-op-complete
   conn
   {:op "info" :symbol symbol :ns ns}
   (fn [fragments]
     (let [last-status (:status (last fragments))
           no-info? (contains? last-status :no-info)]
       (if no-info? :no-info
           (map #(select-keys % [:name :ns]) fragments))))))

(defn stacktrace
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
  [conn]
  (nrepl-op-complete
   conn
   {:op "stacktrace"}
   (fn [fragments]
     (let [stacktraces (->> (map :stacktrace fragments)
                            (remove nil?))] ; done message does not have stacktrace normally
       (apply concat stacktraces)))))

(comment

  (doc-string 'print-table 'clojure.pprint #(println "docs: " %1))


;  
  )