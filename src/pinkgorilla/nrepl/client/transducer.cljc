(ns pinkgorilla.nrepl.client.transducer
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

(defn res-for-req-eval
  "transducer for nrepl-req :eval
   combines multiple res/eval responses 
   into a result.
   will only raise the final result
   "
  [req]
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

(defn done? [res]
  (let [{:keys [status]} res
        done (or (contains? status :done) ;; res status
                 (some #(= "done" %) status))]
    ;(debugf "status: %s done: %s" status done)
    done))


(defn res-for-req-join
  "transducer - joins a req with multiple res
   into a result.
   will only raise the final result
   "
  []
  (fn [xf]
    (let [req? (fn [msg] (:op msg))
          id (fn [msg]
               (-> msg
                   :id
                   keyword))
          reqs (atom {})]
      (fn
        ; init
        ([]
         (reset! reqs {})
         (xf))
        ; stop
        ([result]
         (reset! reqs {})
         (xf (xf result)))
        ; event
        ([result msg]
         (println  "id:" (id msg) "msg: " msg)
         (when-let [mid (id msg)]
           (if (req? msg)
             (do
               (swap! reqs assoc mid {:req msg :res []})
               (xf result (get @reqs mid)))
             (do
               (swap! reqs assoc-in [mid :res] (conj (get-in @reqs [mid :res]) msg))
               (xf result (get @reqs mid))
               (when (done? msg)
                 (swap! reqs dissoc mid)))))
         result)))))

(comment

  (into [] (res-for-req-eval {:op :eval :id 1})
        [{:id 1 :out "1" :ns "user" :value 7}
         {:id 1 :out "2"}
         {:id 1 :out "3"}
         {:id 1 :out "4" :ns "yuppi" :value 9}
         {:id 1 :out "5"}])


;
  )

