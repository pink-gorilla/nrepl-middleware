(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn]]
      :clj [taoensso.timbre :refer [debug info warn]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   ; side-effects:
   [pinkgorilla.nrepl.client.op.eval]))


(defn process-fragment-log [process-fragment result fragment]
  (let [r (process-fragment result fragment)]
  (info "processing: " fragment "result: " r)
    r
  ))


(defn process-req [{:keys [req fragments]}]
  (let [{:keys [initial-value process-fragment]} (init req)
        p (partial process-fragment-log process-fragment)
        ]
    (reduce p initial-value fragments)))

