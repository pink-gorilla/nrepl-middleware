(ns pinkgorilla.nrepl.logger
  (:require
   [pinkgorilla.nrepl.ignore :refer [ignore?]]))

(defn cut-namespaces [msg]
  (if (get-in msg [:value :namespace-definitions])
    (dissoc msg :value)
    msg))

(defn resp-safe [resp]
  (-> resp
      cut-namespaces
      (dissoc  :session ; session-id
               :nrepl.middleware.print/keys
               :changed-namespaces)))

(defn max-code [msg]
  (let [code (:code msg)
        c (if code (count code) 0)
        long? (> c 240)
        code (if long? (subs code 0 240) code)]
    (if code (assoc msg :code code) msg)))

(defn msg-safe [msg]
  (-> msg
      (max-code)
      (dissoc :session ; session-id
              :transport
              :file :line :column
                ;:stdout
              :stderr
              :pprint
              :nrepl.middleware.print/keys
              :nrepl.middleware.print/print-fn
              :nrepl.middleware.print/print
              :nrepl.middleware.print/options
              :nrepl.middleware.caught/caught-fn)))

(def log-file "target/nrepl.txt")

(defn log-resp [req resp]
  (when (not (ignore? req resp))
    (spit log-file
          (str "\r\n\r\n" "req " (pr-str (msg-safe req))
               "\r\n" "res " (pr-str (resp-safe resp)))
          :append true)))

(defn log-req [req]
  (spit log-file
        (str "\r\n\r\n req-only" (pr-str req))
        :append true))

(defn log [text]
  (spit log-file
        (str "\r\n" text)
        :append true))