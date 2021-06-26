(ns demo.data.notebook)

(def notebook
  {:meta {:id :7c9ab23f-c32f-4879-b74c-de7835ca8ba4
          :title "demo 123"
          :tags #{:demo :simple}}
   :segments
   [{:id 1
     :type :code
     :data {:kernel :clj
            :code (str "(require '[pinkgorilla.nrepl.client.core :as cc])\n"
                       ; "(require '[pinkgorilla.nrepl.client.connection :as c])\n"
                       "(require '[clojure.core.async :as async :refer [<!! go]])")}
     :state nil}
    {:id 2
     :type :code
     :data {:kernel :edn
            :code "13"}
     :state {:id 15
             :picasso {:type :hiccup
                       :content [:span {:class "clj-long"} 13]}}}
    {:id 3
     :type :code
     :data {:kernel :clj
            :code (str "(def conn (cc/connect {:port 9100 :host \"localhost\"}))\n"
                       "(defn r! [op] (cc/send-request-sync! conn op))")}}

    {:id 4
     :type :code
     :data {:kernel :clj
            :code "(r! {:op \"describe\"})"}}

]})
