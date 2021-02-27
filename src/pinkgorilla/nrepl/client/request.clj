(ns pinkgorilla.nrepl.client.request
  (:require
   [clojure.pprint :refer [pprint]]
   [nrepl.core :as nrepl]))

(defn- add-session-id [state msg]
  (if-let [session-id (:session-id @state)]
    (assoc msg :session session-id)
    msg))

(defn request!
  "makes a nrepl request.
   waits until all responses are received
   returns the fragments
   "
  [state msg]
  ;(println "send! msg" msg " state: " @state " fn: " on-receive-fn)
  (if-let [client (:client @state)]
    (->> (add-session-id state msg)
         (nrepl/message client)
         doall)
    (do
      (println "cannot send nrepl msg. not connected!")
      nil)))

(defn request-rolling!
  "make a nrepl request ´msg´ and for each partial reply-fragment
   execute ´fun´"
  [state msg fun]
  (if-let [client (:client @state)]
    (loop [fragments (client msg)]
      (let [f (take 1 fragments)]
        (fun f)
        (recur (rest fragments))))
    (println "cannot send nrepl msg. not connected!")))

