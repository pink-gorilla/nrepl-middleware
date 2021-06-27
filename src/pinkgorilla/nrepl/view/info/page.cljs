(ns pinkgorilla.nrepl.view.info.page
  (:require
   [taoensso.timbre :refer-macros [infof]]
   [re-frame.core :refer [subscribe]]
   [pinkgorilla.nrepl.view.info.connect :refer [connect-ui]]
   [pinkgorilla.nrepl.view.info.panel :refer [info-panel]]
   [pinkgorilla.nrepl.view.info.events] ; side-effects
   [pinkgorilla.nrepl.view.op] ; side-effects
   ))

(defn nrepl-info []
  (let [ninfo (subscribe [:nrepl/info])
        nconn (subscribe [:nrepl/conn])]
    (fn []
      (let [{:keys [describe sessions middleware sniffer-status]} @ninfo
            {:keys [session-id]} @nconn
            ;_ (infof "nrepl session %s info: %s" session-id describe)
            ]
        [:div
         [connect-ui]
         [info-panel session-id describe sessions middleware sniffer-status]]))))

