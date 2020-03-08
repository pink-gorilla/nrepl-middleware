(ns pinkgorilla.middleware.formatter
  (:require
   [clojure.data.json :as json]
   #_[cheshire.core :as json])
  #_(:refer [clojure.data.json :rename {write-str generate-string}]))



#_(defn serialize
  "Default JSON serializer."
  [val]
  (json/write-str val))

(defn serialize
  "Default EDN serializer."
  [val]
  (pr-str val))