(ns pinkgorilla.middleware.formatter)

(defn serialize
  "Default EDN serializer."
  [val]
  (pr-str val))