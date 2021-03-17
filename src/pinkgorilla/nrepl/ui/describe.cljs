(ns pinkgorilla.nrepl.ui.describe)

(defn describe [res-describe]
  (let [server-description (first res-describe)]
    (if server-description
      (let [nrepl-version (get-in server-description [:versions :nrepl :version-string])
            clj-version (get-in server-description [:versions :clojure :version-string])
            java-version (get-in server-description [:versions :java :version-string])
            ;cider-version (get-in server-description [:aux :cider-version :version-string])
            ops (get-in server-description [:ops])]
        [:div
         [:p "Java" java-version]
         [:p "Clojure" clj-version]
         [:p (str " nREPL " nrepl-version)]
         ;[:p (str " Cider " cider-version)] ; nrepl 0.8 does not provide cider info ???
         [:p "OPs" (str (keys ops))]])
      [:div "No desc rcvd!"])))



