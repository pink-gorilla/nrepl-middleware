(ns pinkgorilla.nrepl.view.info.events
  (:require
   [re-frame.core :as rf]
   [pinkgorilla.nrepl.client.core :refer [op-describe op-lssessions op-lsmiddleware op-eval]]))

(rf/reg-sub
 :nrepl/info
 (fn [db _]
   (let [info (or (get-in db [:nrepl/info])
                  {:sessions nil  ; this needs to be here, otherwise ops fail
                   :describe nil})]
     info)))

(rf/reg-event-fx
 :nrepl/info-get
 (fn [cofx [_]]
   (rf/dispatch [:nrepl/describe])
   (rf/dispatch [:nrepl/ls-sessions])
   (rf/dispatch [:nrepl/ls-middleware])
   ;(rf/dispatch [:nrepl/sniffer-status])
   (rf/dispatch [:nrepl/eval-test "(+ 1 1)"])))

(rf/reg-event-fx
 :nrepl/describe
 (fn [cofx [_]]
   (rf/dispatch [:nrepl/op-db (op-describe) [:nrepl/info :describe]])))

(rf/reg-event-fx
 :nrepl/ls-sessions
 (fn [cofx [_]]
   (rf/dispatch [:nrepl/op-db (op-lssessions) [:nrepl/info]])))

(rf/reg-event-fx
 :nrepl/ls-middleware
 (fn [cofx [_]]
   (rf/dispatch [:nrepl/op-db (op-lsmiddleware) [:nrepl/info]])))

#_(rf/reg-event-fx
   :nrepl/sniffer-status
   (fn [cofx [_]]
     (rf/dispatch [:nrepl/op-db {:op "sniffer-status"} [:nrepl/info]])))

(rf/reg-event-fx
 :nrepl/eval-test
 (fn [_ [_ code result-db-path]]
   ;(infof ":nrepl/eval code: code db-path: %s"  code result-db-path)
   (rf/dispatch [:nrepl/op-db
                 (op-eval code)
                 [:nrepl/info :eval-test]])))



