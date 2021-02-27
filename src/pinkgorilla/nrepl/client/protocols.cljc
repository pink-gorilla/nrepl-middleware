(ns pinkgorilla.nrepl.client.protocols)


#?(:clj (defmulti init (fn [req] (:op req)))
   :cljs (defmulti init (fn [req] (:op req))))



