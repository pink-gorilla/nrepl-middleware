(ns demo.page.notebook
  (:require
   [re-frame.core :as rf]
   [webly.web.handler :refer [reagent-page]]
   [demo.data.notebook :as data] ; sample-data
   [ui.notebook.core :refer [notebook-view]]
   [demo.site :refer [template-header-document menu]]))

;(rf/dispatch [:css/set-theme-component :codemirror "base16-light"])
(rf/dispatch [:css/set-theme-component :codemirror "mdn-like"])
(rf/dispatch [:doc/load data/notebook])

(def opts
  {; if a layout option is passed this will override the settings in localstorage
   ;:layout :single ; :vertical ; :horizontal
   :view-only false})

(defmethod reagent-page :demo/notebook [{:keys [route-params query-params handler] :as route}]
  [template-header-document
   [menu]
   [notebook-view opts]])


(rf/reg-event-fx
 :document/new
 (fn [_ [_]]
   (rf/dispatch [:doc/new])))
