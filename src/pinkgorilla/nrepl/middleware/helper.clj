

#_(defn send-to-pinkie! [{:keys [code] :as req} {:keys [value] :as resp}]
    (when (and code true); (contains? resp :value))
      (println "evalpinkie:" (read-string code) value))
    resp)