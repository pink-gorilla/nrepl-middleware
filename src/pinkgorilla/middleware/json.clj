(ns pinkgorilla.middleware.json
  (:require
   [clojure.data.json :as json]
   #_[cheshire.core :as json])
  #_(:refer [clojure.data.json :rename {write-str generate-string}]))

(defn serialize
  "Default JSON serializer."
  [val]
  (json/write-str val))