(ns pinkgorilla.nrepl.view.info.connect
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defn connect-ui []
  (let [nrepl (rf/subscribe [:nrepl/status])
        ws-url (r/atom (:ws-url @nrepl))] ;ws-url allows user o change url
    (fn []
      (let [{:keys [connected?]} @nrepl]
        (if connected?
          [:div.border.border-red-500
           [:p.text-green-800 "Connected to: " @ws-url]
           [:button.bg-green-400 {;:type "button"
                                  :on-click #(rf/dispatch [:nrepl/connect])} "connect again"]]
          [:div.border.border-red-500
           [:h1.text-xl "connect to nrepl relay"]
           [:span "NRepl Relay url:"]
           [:input.ml-5.mr-5 {:style {:min-width "300px"}
                              :value @ws-url
                              :on-change (fn [evt]
                                           (let [v (-> evt .-target .-value)]
                                             (reset! ws-url v)))}]
           [:button.bg-green-400 {;:type "button"
                                  :on-click #(rf/dispatch [:nrepl/connect-to @ws-url])} "connect"]])))))

