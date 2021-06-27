(ns demo.page.main
  (:require
   [taoensso.timbre :refer-macros [debug info warn error]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [webly.web.handler :refer [reagent-page]]))

(defn link-fn [fun text]
  [:a.bg-blue-300.cursor-pointer.hover:bg-red-700.m-1
   {:on-click fun} text])

(defn link-dispatch [rf-evt text]
  (link-fn #(rf/dispatch rf-evt) text))

(defn link-href [href text]
  [:a.bg-blue-300.cursor-pointer.hover:bg-red-700.m-1
   {:href href} text])

(defn block [& children]
  (into [:div.bg-blue-400.m-5.inline-block {:class "w-1/4"}]
        children))

(defmethod reagent-page :demo/main [& args]
  [block
   [:p.text-4xl "nrepl demo"]
   [:p [link-dispatch [:reframe10x-toggle] "tenx-toggle"]]
   [:p [link-dispatch [:bidi/goto :demo/ops] "ops"]]
   [:p [link-dispatch [:bidi/goto :demo/notebook] "notebook"]]
   [:p [link-dispatch [:bidi/goto :demo/info] "nrepl info"]]])

