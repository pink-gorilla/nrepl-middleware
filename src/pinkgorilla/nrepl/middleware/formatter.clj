(ns pinkgorilla.nrepl.middleware.formatter)

(defn pr-str-with-meta [data]
  (binding [*print-meta* true]
    (pr-str data)))

(defn serialize
  "Default EDN serializer."
  [val]
  (pr-str-with-meta val))