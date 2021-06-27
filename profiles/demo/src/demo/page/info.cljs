(ns demo.page.info
  (:require
   [taoensso.timbre :refer-macros [debug info warn error]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [webly.web.handler :refer [reagent-page]]
   [pinkgorilla.nrepl.view.info.page :as page]
   ))




(defmethod reagent-page :demo/info [& args]
  [page/nrepl-info])
