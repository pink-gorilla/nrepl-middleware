(ns demo.views
  (:require
   [taoensso.timbre :refer-macros [debug info warn error]]))


(defn nrepl-conn-info [conn]
  (let [{:keys [connected? session-id]} @conn]
    [:div
     [:p "NRepl connected:" (str connected?)]
     [:p "NRepl session-id:" (str session-id)]]))

(defn misc [data]
  [:div
   (doall (for [[k v] data]
            ^{:key k}
            [:div
             [:p [:b (str k)]]
             [:p (str v)]]))])


(defn link-href [href text]
  [:a.bg-blue-300.cursor-pointer.hover:bg-red-700.m-1
   {:href href} text])

(defn app [conn data]
  [:div
    [link-href "/" "main"]
   [:h1 "NRepl demo"]
   [:p "config" (pr-str (:config conn))]
   [:h2 "Will connect to nrepl-ws relay, and then run a few commands and print them."]
   [:h2 "Please start ws relay with 'lein relay-jetty' "]
   [nrepl-conn-info (:conn conn)]
   [misc @data]
   [:p "You can also look into your browser console"]])