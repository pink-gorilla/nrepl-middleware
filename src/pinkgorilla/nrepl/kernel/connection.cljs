(ns pinkgorilla.nrepl.kernel.connection
  (:require
   [taoensso.timbre :refer-macros [debug info infof warnf warn error errorf]]
   ;[reagent.core :as r]
   ;[reagent.ratom :refer [make-reaction]]
   [re-frame.core :as rf]
   [pinkgorilla.nrepl.client.core :refer [connect send-request! request-rolling!]]))

;; re-frame bridge

(rf/reg-sub
 :nrepl/status
 (fn [db [_]]
   (let [s (or (:nrepl/status db)
               {:connected? false
                :conn nil})]
     s)))

(rf/reg-sub
 :nrepl/connected?
 (fn [db _]
   (get-in db [:nrepl/status :connected?])))

(rf/reg-sub
 :nrepl/conn
 (fn [db _]
   (let [conn-a (get-in db [:nrepl/status :conn :conn])]
     (when conn-a
       @conn-a))))

(rf/reg-event-db
 :nrepl/set-connection-status
 (fn [db [_ connected?]]
   (let [old-connected? (get-in db [:nrepl/status :connected?])]
     (if (= old-connected? connected?)
       (do (warnf ":nrepl/set-connection-status - connection status unchanged: %s" connected?)
           db)
       (do
         (if connected?
           (do
             (info "nrepl connected!")
             (rf/dispatch [:nrepl/register-sniffer-sink]))
           (info "nrepl disconnected!"))
         (assoc-in db [:nrepl/status :connected?] connected?))))))

(defn start-bridge [conn]
  (debug "start nrepl bridge: " @conn)
  (add-watch
   conn
   :my-watch
   (fn [c]
     (let [{:keys [connected?]} @conn]
       (error "nrepl ws connected? " connected?)
       (rf/dispatch [:nrepl/set-connection-status connected?])))))

;; connetion management

(rf/reg-event-db
 :nrepl/connect
 (fn [db [_ nrepl-config]]
   (infof "starting nrepl client connection: %s" nrepl-config)
   (let [{:keys [conn] :as c} (connect nrepl-config)]
     (start-bridge conn)
     (assoc-in db [:nrepl/status :conn] c))))

(rf/reg-event-fx
 :nrepl/init
 (fn [{:keys [db] :as cofx} _]
   (let [nrepl-config (or (get-in db [:config :nrepl]) {})
         {:keys [enabled]} nrepl-config]
     (if enabled
       (rf/dispatch [:nrepl/connect nrepl-config])
       (error "nrepl is disabled."))
     nil)))

(def nrepl-conn (rf/subscribe [:nrepl/status]))









