(ns demo.site
  (:require
   [re-frame.core :as rf]
   [picasso.data.notebook :as data]
   [ui.notebook.menu]))

(defn link-fn [fun text]
  [:a.bg-blue-300.cursor-pointer.hover:bg-red-700.m-1
   {:on-click fun} text])

(defn link-dispatch [rf-evt text]
  (link-fn #(rf/dispatch rf-evt) text))

(defn link-href [href text]
  [:a.bg-blue-300.cursor-pointer.hover:bg-red-700.m-1
   {:href href} text])

(defn menu []
  [:div
   [link-href "/" "main"]
   [link-dispatch [:doc/load data/notebook] "load"]
   [link-dispatch [:notebook/template] "template"]
   [link-dispatch [:notebook/move :to 1] "activate 1"]
   [link-dispatch [:notebook/move :to 8] "activate 8"]
   [link-dispatch [:segment-active/eval] "eval active "]
   [link-dispatch [:segment/new-above] "new above"]
   [link-dispatch [:segment/new-below] "new below"]
   [link-dispatch [:segment-active/delete] "delete active"]

   [ui.notebook.menu/menu]])

(defn template-header-document [header document]
  [:div {:style {:display "grid"
                 :height "100vh"
                 :width "100vw"
                 :grid-template-columns "auto"
                 :grid-template-rows "30px auto"}}
   header
   ;[:div.overflow-auto.m-0.p-0
   ; {:style {:background-color "red"
   ;          :height "100%"
    ;         :max-height "100%"}}
   document]
;  ]
  )