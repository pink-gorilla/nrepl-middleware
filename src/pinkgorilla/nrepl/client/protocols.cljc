(ns pinkgorilla.nrepl.client.protocols)

; init: fn that depending on :op returns
; a different map: 
; {:inital-value xxx
;  :process-fragment (fn [result fragment])
;  } 
#?(:clj (defmulti init (fn [req] (:op req)))
   :cljs (defmulti init (fn [req] (:op req))))