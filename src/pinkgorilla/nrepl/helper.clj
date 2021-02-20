(ns pinkgorilla.nrepl.helper
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.core.async :refer [chan >! go]]))

(defn port-from-file []
  (try
    (Integer/parseInt (slurp ".nrepl-port"))
    (catch clojure.lang.ExceptionInfo e 0)
    (catch Exception e 0)))

(defn print-eval-result [fragments]
  (doall
   (map-indexed
    (fn [i f]
      (println i ": " (dissoc f :id #_:session))) fragments))
  fragments)

(defn status [fragments]
  (:status (last fragments)))

(defn success? [fragments]
  (let [s (status fragments)
        e (filter #(= "error" %) s)]
    (= 0 (count e))))