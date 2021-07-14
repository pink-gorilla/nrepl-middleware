(ns pinkgorilla.nrepl.view.info.page
  (:require
   [taoensso.timbre :refer-macros [infof]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [pinkgorilla.nrepl.view.info.connect :refer [connect-ui]]
   [pinkgorilla.nrepl.view.info.panel :refer [info-panel]]
   [pinkgorilla.nrepl.view.info.events] ; side-effects
   [pinkgorilla.nrepl.view.op] ; side-effects
   ))
(defn nrepl-info []
  (let [first (r/atom true)
        ninfo (rf/subscribe [:nrepl/info])
        nconn (rf/subscribe [:nrepl/conn])]
    (fn []
      (let [{:keys [describe sessions middleware sniffer-status]} @ninfo
            {:keys [session-id]} @nconn
            _ (when @first
                (reset! first false)
                (rf/dispatch [:nrepl/info-get]))
            ;_ (infof "nrepl session %s info: %s" session-id describe)
            ]
        [info-panel session-id describe sessions middleware sniffer-status]))))

(defn nrepl-info-connect []
  [:div
   [connect-ui]
   [:button.bg-green-400 {;:type "button"
                          :on-click #(rf/dispatch [:nrepl/info-get])} "get info"]

   [nrepl-info]])

