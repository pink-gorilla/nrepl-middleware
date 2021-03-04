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
        ([acc]
         (let [r @reqs]
           (reset! reqs nil)
           (xf (xf acc r))))
        ([acc res]
         (swap! reqs #(process-fragment % res))
         acc)))))

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
    (let [req? (fn [msg] (:op-kw msg))
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
        ([acc]
         (reset! reqs {})
         (xf (xf acc)))
        ; event
        ([acc msg]
         ;(println  "id:" (id msg) "msg: " msg)
         (when-let [mid (id msg)]
           (if (req? msg)
             (do
               (swap! reqs assoc mid {:req msg :res []})
               (xf acc (get @reqs mid)))
             (do
               (swap! reqs assoc-in [mid :res] (conj (get-in @reqs [mid :res]) msg))
               (xf acc (get @reqs mid))
               (when (done? msg)
                 (swap! reqs dissoc mid)
                 ;(println "remaining: " (keys @reqs))
                 ))))
         acc)))))



(defn res-transform-conj
  "transducer - conj multiple res into a result."
  [partial?]
  (fn [xf]
    (let [req0 (atom nil)
          ress (atom [])]
      (fn
        ; init
        ([]
         (reset! req0 nil)
         (reset! ress [])
         (xf))
        ; stop
        ([acc]
         (when-not partial?
           (xf acc {:req @req0 :res @ress}))
         (reset! req0 nil)
         (reset! ress [])
         (xf (xf acc)))
        ; event
        ([acc req-res]
         ;(println  "id:" (id msg) "msg: " msg)
         (let [{:keys [req res]} req-res]
           (when req
             (reset! req0 req))
           (when res
             (swap! ress conj res))
           (when partial?
             (xf acc {:req @req0 :res @ress})))
         acc)))))

(defn op-kw [req-res]
  (let [op (get-in req-res [:req :op])]
    (pr-str "op: " op)
    (if (keyword? op)
      (:req req-res)
      (:req (assoc-in req-res [:req :op] (keyword op))))))

(defn res-extract
  "transducer - extracts (reduces) useful information
   from multiple res into a result."
  []
  (fn [xf]
    (let [parser (atom nil)
          result (atom nil)]
      (fn
        ; init
        ([]
         (reset! result nil)
         (xf))
        ; stop
        ([acc]
         (reset! result nil)
         (xf (xf acc)))
        ; event
        ([acc req-res]
         ;(println  "id:" (id msg) "msg: " msg)
         (when-not @parser
           (let [{:keys [initial-value process-fragment]} (init (op-kw req-res))]
             (reset! parser process-fragment)
             (reset! result initial-value)))
         (when (:res req-res)
           (swap! result #(@parser % (:res req-res))))
         (println "result:" @result)
         (xf acc {:req (:req req-res) :res @result})
         acc)))))

(comment

  (into [] (res-for-req-eval {:op-kw :eval :id 1})
        [{:id 1 :out "1" :ns "user" :value 7}
         {:id 1 :out "2"}
         {:id 1 :out "3"}
         {:id 1 :out "4" :ns "yuppi" :value 9}
         {:id 1 :out "5"}])


;
  )

