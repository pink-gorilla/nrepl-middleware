(ns pinkgorilla.nrepl.client.transducer
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

(defn done? [res]
  (let [{:keys [status]} res
        done (or (contains? status :done) ;; res status
                 (some #(= "done" %) status))]
    ;(debugf "status: %s done: %s" status done)
    done))

(defn op-kw [req-res]
  (let [op (get-in req-res [:req :op])]
    (pr-str "op: " op)
    (if (keyword? op)
      (:req req-res)
      (:req (assoc-in req-res [:req :op] (keyword op))))))

(defn id-kw [msg]
  (-> msg
      :id
      keyword))

(defn req-res-join
  "transducer - joins a req with multiple res
   into a result. To achieve this, it saves the
   state of open requests (stateful transducer). 

   will only raise the final result
   "
  [transform partial?]
  (fn [xf]
    (let [req? (fn [msg] (:op msg))
          reqs (atom {})]
      (fn
        ; init
        ([]
         (println "init")
         (reset! reqs {})
         (xf))
        ; stop
        ([acc]
         (reset! reqs {})
         (xf (xf acc)))
        ; event
        ([acc msg]
         ;(println  "id:" (id msg) "msg: " msg)
         (when-let [mid (id-kw msg)]
           (if (req? msg)
             (do
               ;(println "msg-req: " msg)
               (let [t ((transform partial?) xf)]
                 (swap! reqs assoc mid {:t t :req msg})
                 (t) ; start
                 (t acc {:req msg}) ; process
               ;(xf acc (get @reqs mid))
                 ))
             (let [{:keys [t req]} (get @reqs mid)]
               (t acc {:req req :res msg})
               (when (done? msg)
                 (swap! reqs dissoc mid)
                 (t acc) ; stop
                 ;(println "remaining: " (keys @reqs))
                 ))))
         acc)))))

(defn res-transform-conj
  "transducer - conj multiple res into a result.
   Useful for logging / testing.
   "
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
;; => #'pinkgorilla.nrepl.client.transducer/res-transform-conj


(defn res-transform-extract
  "transducer - extracts (reduces) useful information
   from multiple res into a result. Used to build a 
   notebook."
  [partial?]
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
         (when-not partial?
           (xf acc @result))
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
         ;(println "result:" @result)
         (when partial?
           (xf acc {:req (:req req-res) :res @result}))
         acc)))))



