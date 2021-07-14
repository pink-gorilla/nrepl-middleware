(ns pinkgorilla.nrepl.client.connection.sente
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]]
   [taoensso.timbre :refer-macros [tracef debug debugf info warn error]]
   [reagent.core :refer [atom]]
   [re-frame.core :as rf]
   [webly.ws.msg-handler :refer [-event-msg-handler]]
   [webly.ws.core :refer [send!]]
   [pinkgorilla.nrepl.client.id :refer [guuid]]))

(def req-ch (chan))
(def res-ch (chan))

;tis does not work. not sure why.
#_(defmethod -event-msg-handler :nrepl/res
    [{:as ev-msg :keys [event id ?data]}]
    (let [[_ res] event] ; _ is :nrepl/req
      (info "nrepl res rcvd: " res)
      (go
        (>! res-ch res))))

(rf/reg-event-fx
 :nrepl/res
 (fn [_ [_ res]] ; _ {:keys [db] :as cofx} _ :nrepl/res
   (error "nrepl res rcvd: " res)
   (go
     (>! res-ch res))
   nil))

(defn connect!
  [_]
  (let [conn (atom {:session-id nil  ; sent from nrepl on connect, set by receive-msgs!
                    :req-ch req-ch
                    :res-ch res-ch
                    :connected? true})]
    (error "starting nrepl sente relay!")
    (go-loop []
      (let [req (<! req-ch)]
        (info "relaying nrepl req: " req)
        (send! [:nrepl/req req])
        (recur)))

    conn))

(defn disconnect! [{:keys [conn] :as s}]
 ; (let [transport (:transport @conn)]
  (info "disconnecting sente nrepl session. not implemented."))
