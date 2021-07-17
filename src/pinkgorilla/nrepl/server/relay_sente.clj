(ns pinkgorilla.nrepl.server.relay-sente
  (:require
   [clojure.string]
   [taoensso.timbre :as log :refer [debug info infof warn error errorf]]
   [webly.ws.core :refer [send-response]]
   [webly.ws.msg-handler :refer [-event-msg-handler]]
   [pinkgorilla.nrepl.client.core :refer [connect request-rolling!]]))

(def conn (atom nil))
(defn start-sente-relay []
  (info "starting nrepl sente relay")
  (reset! conn (connect {:type :in-process})))

(comment
  ; ev-msg
  {:client-id "819c92ec-da42-417c-845c-883b63cd123e"
   :uid "685e0c86-120d-4c5e-adde-01773c9b9748"
   :event [:nrepl/req {:op "ls-sessions"
                       :id "478db1fb-4355-4419-999c-0f3324bfac3c"}]
   :id :nrepl/req})
(defn relay-responses [ev-msg req]
  (if @conn
    (let [uid (:uid ev-msg)]
      (infof "relaying nrepl req: %s to: %s " req uid)
      (request-rolling! @conn req
                        (fn [res]
                          (infof "relaying to: %s nrepl res: %s" uid res)
                          (send-response ev-msg :nrepl/res res))
                        :raw))
    (error "sente relay not started! cannot relay nrepl reqs")))

(defmethod -event-msg-handler :nrepl/req
  [{:as ev-msg :keys [event id ?data]}]
  (let [[_ req] event] ; _ is :nrepl/req
    (relay-responses ev-msg req)))