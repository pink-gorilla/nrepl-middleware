(ns src-old.ws-url)

   [clojure.string :as str]
[cemerick.url :as url]
(defn application-url []
  (url/url (-> js/window .-location .-href)))

(defn ws-path [port path]
  (let [app-url (application-url)
        proto (if (= (:protocol app-url) "http") "ws" "wss")
        port (or port (:port app-url))
        port-postfix (if (< 0 port)
                       (str ":" port)
                       "")
        wsp (str proto "://" (:host app-url) port-postfix path)]
    (info "nrepl ws-endpoint: " wsp)
    wsp))
   

#_(reg-event-db
   :nrepl/connect-to
   (fn [db [_ ws-url]]
     (let [db (or db {})]
       (dispatch [:nrepl/connect])
       (assoc-in db [:nrepl :ws-url] ws-url))))



(rf/reg-event-db
 :nrepl/init
 (fn [db [_]]
   (let [db (or db {})
         api (get-in db [:config :profile :server :api])
         port-api (get-in db [:config :web-server-api :port])
         port (when api port-api)
         config (get-in db [:config :nrepl])
         {:keys [ws-endpoint connect?]} config
         ws-endpoint (or ws-endpoint (ws-path port "/api/nrepl"))]
     (if connect?
       (do
         (warn "auto-connect nrepl: " ws-endpoint)
         (rf/dispatch [:nrepl/connect]))
       (warn "NOT connecting automatically to nrepl"))
     (assoc db :nrepl/status {;:ws-url ws-endpoint
                              :connected? false
                              :conn nil}))))