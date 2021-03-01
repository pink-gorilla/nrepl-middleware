(ns pinkgorilla.nrepl.client.transducer
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

(defn process-req [req]
  (fn [xf]
    (let [{:keys [initial-value process-fragment]} (init req)
          reqs (atom initial-value)]
      (fn
        ([]
         (xf))
        ([result]
         (let [r @reqs]
           (reset! reqs nil)
           (xf (xf result r))))
        ([result res]
         (swap! reqs #(process-fragment % res))
         result)))))

(comment

  (into [] (process-req {:op :eval :id 1})
        [{:id 1 :out "1" :ns "user" :value 7}
         {:id 1 :out "2"}
         {:id 1 :out "3"}
         {:id 1 :out "4" :ns "yuppi" :value 9}
         {:id 1 :out "5"}])

;
  )

