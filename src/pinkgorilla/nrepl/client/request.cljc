(ns pinkgorilla.nrepl.client.request
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [tracef debug debugf info infof warn error errorf]]
      :clj [taoensso.timbre :refer         [tracef debug debugf info infof warn error errorf]])
   #?(:cljs [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]]
      :clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop]])
   #?(:cljs [reagent.core :refer [atom]])
   [pinkgorilla.nrepl.client.id :refer [guuid]]
   [pinkgorilla.nrepl.client.protocols :refer [init]]))


; multiplexer


(defn- log-msg [msg]
  (tracef "res - multiplexer: %s" msg))

(defn req-id-kw [res]
  (let [req-id (:id res)
        req-id (if (keyword? req-id) req-id (keyword req-id))]
    req-id))

(defn- process-res [mx res]
  (let [req-id (req-id-kw res)
        request-processor (get @mx req-id)]
    (if request-processor
      (if-let [p (:process-response request-processor)]
        (p res)
        (errorf "no response processing fn: %s" request-processor))
      (errorf "no req-processor registered for req-id: %s resp: %s" req-id res))))

(defn create-multiplexer!
  "reads from nrepl-res-ch in a go-loop
   calls req-processors for processing"
  [conn]
  (let [mx (atom {}) ; keys: request-id, vals: request-state
        res-ch (:res-ch @conn)]
    ; process incoming responses from res-ch
    (go-loop []
      (if-let [res (<! res-ch)]
        (do
          (log-msg res)
          (process-res mx res))
        (do
          (error "res ch result empty. res-ch closed?")
          (<! (timeout 3000))))
      (recur))
    mx))


; request


(defn add-req-processor [mx req-id rps]
  (swap! mx assoc req-id rps))

(defn remove-req-processor [mx req-id]
  (swap! mx dissoc req-id))

(defn done? [res]
  (let [{:keys [status]} res
        done (or (contains? status :done) ;; res status
                 (some #(= "done" %) status))]
    ;(debugf "status: %s done: %s" status done)
    done))

(defn process-req-response [mx rps res]
  (debugf "res - processing: %s" res)
  (let [{:keys [request-id result-ch result process-fragment partial-results?]} rps]
    (swap! result process-fragment res)
    (if (done? res)
      (do
        (infof "req done: %s result: %s " request-id @result)
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

(defn- add-id [req]
  (if (:id req)
    req
    (do
      (tracef "adding id to req %s" req)
      (assoc req :id (guuid)))))

(defn- req-valid? [req]
  (let [v (and (:id req) (:op req))]
    (when-not v
      (errorf "invalid req: " req))
    v))

(defn- req-error [reason]
  (errorf "send-request! error: %s" reason)
  (let [ch-err (chan)]
    (close! ch-err)
    ch-err))

(defn send-request!
  [conn mx partial-results? req]
  (let [req (add-id req)
        {:keys [req-ch]} @conn]
    (if req-ch
      (if (req-valid? req)
        (let [result-ch (create-request-processor! mx partial-results? req)]
          (debugf "send-request! req: %s" req)
          (go
            (>! req-ch req))
          result-ch)
        (req-error "no req-ch"))
      (req-error "invalid req"))))

