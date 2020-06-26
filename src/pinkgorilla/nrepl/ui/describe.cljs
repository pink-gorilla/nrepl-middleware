(ns pinkgorilla.nrepl.ui.describe
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   ;[pinkgorilla.nrepl.ws.client :refer [make-request!]]
   ))

(defn describe [server-description]
  (if server-description
    (let [nrepl-version (get-in server-description [:versions :nrepl :version-string])
          clj-version (get-in server-description [:versions :clojure :version-string])
          java-version (get-in server-description [:versions :java :version-string])
          cider-version (get-in server-description [:aux :cider-version :version-string])
          ops (get-in server-description [:ops])]
      [:div
       [:p (str " nREPL " nrepl-version)]
       [:p (str " Cider " cider-version)]
       [:p "Clojure" clj-version]
       [:p "Java" java-version]
       [:p "OPs" (str (keys ops))]
       [:p " Results: Stored in vars *1, *2, *3, an exception in *e"]])
    [:div "No desc rcvd!"]))

#_(defn- async-perform-op [nrepl-client op]
    (let [result-chan (chan)]
      (nrepl/perform-op nrepl-client op #(put! result-chan %))
      result-chan))


