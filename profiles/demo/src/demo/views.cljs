(ns demo.views
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer [debug info warn error]]
   [pinkgorilla.nrepl.ui.describe :refer [describe]]))


(defn nrepl-conn-info [conn]
  (let [{:keys [connected? session-id]} conn]
    [:div
     [:p "NRepl connected:" (str @connected?)]
     [:p "NRepl session-id:" (str @session-id)]]))

(defn app [conn d]
  [:div
   [:h1 "NRepl demo"]
   [:h2 "Will connect to nrepl-ws relay, and then run a few commands and print them."]
   [:h2 "Please start ws relay with 'lein relay' "]
   [nrepl-conn-info conn]
   [describe @d]
   [:p "To see complete output please look into browser console"]
   ])