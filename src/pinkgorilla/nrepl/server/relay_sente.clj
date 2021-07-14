(ns pinkgorilla.nrepl.server.relay-sente
  (:require
   [clojure.string]
   [taoensso.timbre :as log :refer [debug info infof warn error errorf]]
   [webly.ws.core :refer [send! send-all! send-response]]
   [webly.ws.msg-handler :refer [-event-msg-handler]]
   [pinkgorilla.nrepl.client.core :refer [connect request-rolling!]]
     ;[pinkgorilla.nrepl.client.connection.in-process :refer [connect]]
   [pinkgorilla.nrepl.client.request :as r :refer [create-multiplexer!]]))

(info "starting nrepl sente relay")
(def conn (connect {:type :in-process}))

(comment
  ; ev-msg
  {:client-id "819c92ec-da42-417c-845c-883b63cd123e"
   :uid "685e0c86-120d-4c5e-adde-01773c9b9748"
   :event [:nrepl/req {:op "ls-sessions"
                       :id "478db1fb-4355-4419-999c-0f3324bfac3c"}]
   :id :nrepl/req})
(defn relay-responses [ev-msg req]
  (let [req-id (:id req)
        uid (:uid ev-msg)]
    (infof "relaying nrepl req: %s to: %s " req uid)
    (request-rolling! conn req
                      (fn [res]
                      ;(let [res (assoc res :id req-id)]
                        (infof "relaying to: %s nrepl res: " uid res)
                        (send-response ev-msg :nrepl/res res))
                      :raw)))

(defmethod -event-msg-handler :nrepl/req
  [{:as ev-msg :keys [event id ?data]}]
  (let [[_ req] event] ; _ is :nrepl/req
    (relay-responses ev-msg req)))