(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop <!!]]
      :cljs [cljs.core.async :as async :refer   [<! >! chan timeout close!] :refer-macros [go go-loop]])
   #?(:cljs [taoensso.timbre :refer-macros [debug debugf info infof error errorf]]
      :clj [taoensso.timbre         :refer [debug debugf info infof error errorf]])

   [pinkgorilla.nrepl.client.connection :refer [connect! disconnect!]]
   [pinkgorilla.nrepl.client.request :as r :refer [create-multiplexer!]]
   [pinkgorilla.nrepl.client.id :refer [guuid]]

   ; side-effects (register multi-methods)
   [pinkgorilla.nrepl.client.op.eval]
   [pinkgorilla.nrepl.client.op.concat]
   [pinkgorilla.nrepl.client.op.cider]
   [pinkgorilla.nrepl.client.op.admin]
   [pinkgorilla.nrepl.client.op.gorilla]))

(defn inject-session-id [session-id {:keys [session] :as req}]
  (if (and (nil? session) session-id)
    (do (errorf "injecting session-id %s to req" session-id)
        (assoc req :session session-id))
    (do (debugf "NOT injecting session-id %s to req" session-id)
        req)))

(defn send-request! [{:keys [conn mx session-id]} req & [partial-results?]]
  (let [req-with-session-id (inject-session-id @session-id req)
        result-ch (r/send-request! conn mx partial-results? req-with-session-id)]
    result-ch))

(defn set-session-id! [{:keys [session-id] :as conn}]
  (let [id-req-clone (guuid)
        req-clone {:op "clone" :id id-req-clone}]
    (error "creating new nrepl session ..")
    (go
      (let [res-ch (send-request! conn req-clone false)
            res (<! res-ch)
            session-id-new (:new-session res)]
         ;(error "session res:" res "type: " (type res) (pr-str res))
        (errorf "new session id : %s" session-id-new)
        (reset! session-id session-id-new)))))

(defn connect [config]
  (let [conn (connect! config)
        mx (create-multiplexer! conn)
        c {:config config
           :conn conn
           :mx mx
           :session-id (atom nil)}]
    (set-session-id! c)
    c))

(defn disconnect [s]
  (disconnect! s))

#_(defn send-requests!
    [s reqs]
    (go-loop [todo reqs]
      (when-let [req (first todo)]
        (send-request! s req false)
        (recur (rest todo)))))

#?(:clj

   (defn send-request-sync!
     " <!!  only works in clj. It is used to block until chan val arrives"
     [c req]
     (let [result-ch (send-request! c req false)
           r (<!! result-ch)]
       (infof "result: %s result-ch: " r result-ch)
       r))
 ;  
   )

(defn request-rolling!
  "send a nrepl request, and get `rolling` responses
   for each partial reply-fragment callback ´cb´"
  [c req cb & [raw]]
  (if-let [result-ch (send-request! c req (or raw true))]
    (go-loop [result (<! result-ch)]
      (if result
        (do
          (cb result)
          (recur (<! result-ch)))
        (infof "request-rolling! %s finished!" req)))
    (info "cannot send nrepl msg. not connected!")))

; request op helper (is this really needed?)

(defn op-describe []
  {:op "describe"})

(defn op-lssessions []
  {:op "ls-sessions"})

(defn op-lsmiddleware []
  {:op "ls-middleware"})

(defn op-interrupt []
  {:op :interrupt})

;todo:
; op: close
; op: clone

(defn op-eval
  "evals code"
  [code]
  {:op "eval" :code code})

(defn op-eval-picasso
  "evals code"
  ([code]
   {:op "eval" :code code :as-picasso true})
  ([code ns]
   {:op "eval" :ns ns :code code :as-picasso true}))

; cider ops
; https://docs.cider.mx/cider-nrepl/nrepl-api/ops.html

(defn op-ciderversion
  "cider version
   Relies on the cider-nrepl middleware."
  []
  {:op "cider-version"})

(defn op-docstring
  "Queries the REPL server for docs for the given symbol. 
   Relies on the cider-nrepl middleware."
  [symbol ns]
  {:op "complete-doc" :symbol symbol :ns ns})

(defn op-apropos
  "Relies on the cider-nrepl middleware."
  [query]
  {:op "apropos" :query query})

(defn op-completions
  "Query the REPL server for autocompletion suggestions. 
   Relies on the cider-nrepl middleware."
  [symbol ns context]
  {:op "complete" :symbol symbol :ns ns :context context})

(defn op-resolve-symbol
  "resolve a symbol to get its namespace takes the symbol and the namespace 
   that should be used as context.
   Relies on the cider-nrepl middleware."
  [symbol ns]
  {:op "info" :symbol symbol :ns ns})

(defn op-stacktrace
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware."
  []
  {:op "stacktrace"})

; gorilla

(defn op-gorillanav
  [idx k v]
  {:op "gorilla-nav"
   :datafy (pr-str {:idx idx
                    :k k
                    :v v})})

(defn op-snifferstatus
  "gets gorilla sniffer status"
  []
  {:op "sniffer-status"})

(defn op-sniffersink
  "registere as gorilla sniffer sink
   use with request-rolling."
  []
  {:op "sniffer-sink"})

; helper like this might be handy:

#_(defn ^:export nrepl-eval-cb
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

#_(defn ^:export fn-eval
    "executes a clojure ```function-as-string``` (from clojurescript) "
    [conn function-as-string & params]
    (let [_ (info "params: " params)
          code (concat ["(" function-as-string] params [")"])]
      (nrepl-eval conn code)))

