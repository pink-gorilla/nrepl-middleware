(ns pinkgorilla.nrepl.client.op.cider
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn error]]
      :clj [taoensso.timbre :refer [debug info warn error]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [single-key-concat multiple-key-concat]]))

; todo: notebook-ui - remove :candidates key
; "Query the REPL server for autocompletion suggestions. 
;   Relies on the cider-nrepl middleware."
(defmethod init :complete [req]
  (single-key-concat :completions))

;todo notebook-ui: :docstring
; "Queries the REPL server for docs for the given symbol. 
; Relies on the cider-nrepl middleware."
(defmethod init :complete-doc [req]
  (single-key-concat :completion-doc))

;  "resolve a symbol to get its namespace takes the symbol and the namespace 
;   that should be used as context.
;   Relies on the cider-nrepl middleware. 
;   Returns:
;   - the symbol and the symbol's namespace"
(defmethod init :info [req]
  (multiple-key-concat [:name :ns]))

; "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
;  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
(defmethod init :stacktrace [req]
  (single-key-concat :stacktrace))

#_(defmethod init :eval [req]
    {:initial-value {:value []
                     :picasso []
                     :ns nil
                     :out ""
                     :err []
                     :root-ex nil}
     :process-fragment process-fragment})