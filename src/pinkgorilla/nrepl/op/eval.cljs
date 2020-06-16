(ns pinkgorilla.nrepl.op.eval
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [taoensso.timbre :refer-macros [info warn]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout close!]]
   [cljs.reader :refer [read-string]]
   [reagent.core :as r]
   [pinkgorilla.nrepl.ws.client :refer [nrepl-op-complete]]))

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

(defn- on-eval-fragment
  "result is an atom, containing the eval result.
   processes a fragment-response and modifies result-atom accordingly."
  [result {:keys [out err root-ex ns value status] :as message}]
    ; console
  (when out
    (swap! result assoc :console (str (:console @result) out)))
    ;; eval error
  (when err
    (swap! result assoc :ex err))
    ; value /namespace
  (when ns
    (swap! result assoc :namespace ns)
    (swap! result assoc :value (conj (:value @result) (parse-value value)))) ; (parse-value value)

  (when root-ex ;; root exception ?? what is this ?? where does it come from ? cider? nrepl?
    (swap! result assoc :root-ex root-ex)))

(defn ^:export nrepl-eval
  "evaluates a clj-snippet"
  [conn code]
  (let [result (r/atom {:value []
                        :console ""
                        :error nil})]
    (nrepl-op-complete
     conn
     {:op "eval" :code code}
     (fn [fragments]
       (doseq [f fragments]
         (on-eval-fragment result f))
       @result))))

(defn ^:export nrepl-eval-cb
  "evaluates a clojure expression
   returns result via callback
   
   execute this in browser console:
   pinkgorilla.kernel.nrepl.clj_eval_cb (\"(+ 7 9 )\", 
    (function (r) {console.log (\"result!!: \" +r);}))
   "
  [conn snippet cb]
  (let [ch (nrepl-eval conn snippet)]
    (go
      (cb (<! ch)))))

(defn ^:export fn-eval
  "executes a clojure ```function-as-string``` (from clojurescript) "
  [conn function-as-string & params]
  (let [_ (info "params: " params)
        code (concat ["(" function-as-string] params [")"])]
    (nrepl-eval conn code)))

(comment

  (nrepl-eval conn "(+ 5 5)")

; (s*when (some #{"interrupted"} status)
;      (C (s*update-result (P assoc :ename "interrupted"))


  ;
  )
