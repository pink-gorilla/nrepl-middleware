(ns pinkgorilla.nrepl.logger
  (:require
   [clojure.core.async :as async :refer [<! >! >!! chan poll! timeout close! go go-loop]]
   [pinkgorilla.nrepl.ignore :refer [ignore?]]))

(def log-enabled? false)
;(def log-enabled? true)

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

(def messages-chan (chan))

(go-loop []
  (let [text (<! messages-chan)]
    (println text)
    (recur)))

(def log-file "target/nrepl.txt")

(defn log [text]
  (when log-enabled?
    (spit log-file
          (str "\r\n" text)
          :append true))
  (when text
    (>!! messages-chan text))
  nil)

(defn log-resp [req resp]
  (when (not (ignore? req resp))
    (log (str "\r\n req " (pr-str (msg-safe req))
              "\r\n res " (pr-str (resp-safe resp))))))

(defn log-req [req]
  (log (str "\r\n req-only" (pr-str req))))

(log (str "nrepl logger started in: " log-file))
