(ns pinkgorilla.nrepl.client.request
  #?(:cljs (:require-macros
            [cljs.core.async.macros :refer [go go-loop]]))
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug debugf info warn errorf]]
      :clj [taoensso.timbre :refer [debug debugf info warn errorf]])
   #?(:cljs [cljs.core.async :as async :refer [<! >! chan timeout close!]]
      :clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop]])
   [pinkgorilla.nrepl.client.id :refer [guuid]]
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

(defn add-req-processor [mx req-id rps]
  (swap! mx assoc req-id rps))

(defn remove-req-processor [mx req-id]
  (swap! mx dissoc req-id))

(defn done? [res]
  (let [{:keys [status]} res
        done (or (contains? status :done) ;; res status
                 (some #(= "done" %) status))]
    (debugf "status: %s done: %s" status done)
    done))

(defn process-req-response [mx rps res]
  (info "processing req-rep " res)
  (let [{:keys [request-id result-ch result process-fragment partial-results?]} rps]
    (swap! result process-fragment res)
    (if (done? res)
      (do
        (debugf "request %s  done result: %s result-ch: %s" request-id @result result-ch)
        (remove-req-processor mx request-id)
        (go
          (>! result-ch @result)
          (close! result-ch)))
      (when partial-results?
        (go
          (>! result-ch @result))))))

(defn create-request-processor!
  "make-request 
   - sends `message` to websocket (so nrepl/cider can process the request)
   - returns the eval id.
   parameter:
   - state: this gets returned by (ws-start!)
   - message: a nrepl message (with or without request-id)
   - callback: optional callback that return all fragments of a request
   returns:
   - channel with response fragments"
  [mx partial-results? req]
  (debugf "create-request-processor for op: %s partial: %s" (:op req) partial-results?)
  (let [op (:op req)
        req (if (keyword? op)
              req
              (assoc req :op (keyword op)))
        {:keys [initial-value process-fragment]} (init req)
        result (atom initial-value)
        request-id (keyword (:id req))
        result-ch (chan)
        rps {:request-id request-id
             :result result
             :result-ch result-ch
             :process-fragment process-fragment
             :partial-results? partial-results?}
        rps (assoc rps :process-response
                   (fn [res]
                     (process-req-response mx rps res)))]
    (add-req-processor mx request-id rps)
    result-ch))

(defn add-id [req]
  (if (:id req)
    req
    (do
      (debugf "adding id to req %s" req)
      (assoc req :id (guuid)))))

(defn req-valid? [req]
  (let [v (and (:id req) (:op req))]
    (when-not v
      (errorf "invalid request: " req))
    v))

(defn send-request!
  [conn mx partial-results? req]
  (let [req (add-id req)
        {:keys [req-ch]} @conn]
    (if (req-valid? req)
      (let [result-ch (create-request-processor! mx partial-results? req)]
        (debugf "send-request! req: %s result-ch: %s" req result-ch)
        (if req-ch
          (go
            (>! req-ch req))
          (errorf "send-request! cannot send: no req-ch"))
        result-ch)
      (let [ch-err (chan)]
        (close! ch-err)
        ch-err))))

