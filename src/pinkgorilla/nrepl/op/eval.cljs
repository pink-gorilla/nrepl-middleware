(ns pinkgorilla.nrepl.op.nrepl
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout close!]]
   [cljs.reader :refer [read-string]]
   [pinkgorilla.nrepl.ws.client :refer [make-request!]]))






(defn- parse-value
  "converts (:value message)
   nrepl has to serialize the value before it hits clojure.
   So this value gets formatted with edn.
   Since the message is packaged as edn too, we have edn within edn.
   "
  [value]
  (try
    (let [data (read-string value)
          data2 (read-string data)]
    ;(info "value: " value "type " (type value))
    ;(info "converted: " data "type " (type data))
    ; (info "converted2: " data2 "type " (type data2))
    ;(info "converted value-response" (:value-response data2))
      data2)
    (catch js/Error e (info "parse-value ex: " e " tried to parse: " value))))


(defn on-eval-update [context type data]
  (let [{:keys [result channel]} context]
    (case type
      :value (swap! result assoc :value (conj (:value data) data))
      :console (swap! result assoc :console (str (:console @result data) data))
      :error (swap! result assoc :error data)
      :done (do
              (info "clj-eval finished!")
              (if-not (nil? data)
                (put! channel data))))))


(defn on-nrepl-eval-response
  "nepl response parser processes one or moe messages for each evaluation.
   It is used to evaluate notebook segments, 
   and to do cystom evaluations"
  [handle eval-id {:keys [out err root-ex ns value status] :as message}]
  (cond
    ns ;; value response
    (let [data (parse-value value)]
      (handle :value {:data data :ns ns}))

    out ;; console string
    (handle :console out)

    err ;; eval error
    (handle :error err)

    root-ex ;; root exception ?? what is this ?? where does it come from ? cider? nrepl?
    (info "Got root-ex" root-ex "for" eval-id)

    (contains? status :done) ;; eval status
    (do
      (swap! ws-repl dissoc [:callback eval-id])
      (handle :done nil))

    :else
    (info "rcvd unhandled notebook segment message: " message) ;; end of messages that have segment-id
    ))


(defn ^:export eval!
  "evaluates a clj-snippet"
  [state code]
  (let [fragment-ch (make-request! state {:op "eval" :code code})
        atom-result (r/atom {:value []
                             :console ""
                             :error nil})
        
        result-chan (chan)
        context {:channel result-chan
                 :result atom-result}
        handler (partial clj-eval-handler context)
        on-response (partial on-nrepl-eval-response handler eval-id)
        _ ]
    context))


(defn ^:export clj-eval-sync
  "executes a clojure expression
   and returns the result to the ```result-atom```"
  [snippet]
  (let [context (clj-eval! snippet)
        {:keys [channel result]} context]
    (go (<! channel)
        @result)))

(defn ^:export clj-eval-cb
  "executes a clojure expression
   and returns the result to the ```result-atom```
   
   execute this in browser console:
   pinkgorilla.kernel.nrepl.clj_eval_cb (\"(+ 7 9 )\", 
    (function (r) {console.log (\"result!!: \" +r);}))
   "
  [snippet cb]
  (let [context (clj-eval! snippet)
        {:keys [channel result]} context]
    (go (<! channel)
        (cb @result))))


(defn ^:export clj
  "executes a clojure ```function-as-string``` (from clojurescript) 
   and stores the result in ```result-atom```"
  [result-atom function-as-string & params]
  (let [_ (info "params: " params)
        expr (concat ["(" function-as-string] params [")"]) ; params)
        str_eval (clojure.string/join " " expr)
        _ (info (str "Calling CLJ: " str_eval))
        context (clj-eval! str_eval)
        {:keys [channel result]} context]
    (go (_ (<! channel))
        (reset! result-atom @result))))

(defn clj-eval-ignore-result [function-as-string & params]
  (let [result-atom (r/atom {})]
    (if params
      (apply (partial clj result-atom function-as-string)  params)
      (clj result-atom function-as-string))))




(comment

  (eval! 15 "(+ 5 5)")



  ;
  )
