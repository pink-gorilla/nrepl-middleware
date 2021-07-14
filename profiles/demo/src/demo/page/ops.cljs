(ns demo.page.ops
  (:require
   [taoensso.timbre :as timbre :refer-macros [debug info warn error]]
   [cljs.core.async :as async :refer [<!] :refer-macros [go]]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [webly.web.handler :refer [reagent-page]]
   [pinkgorilla.nrepl.client.core :refer [connect send-request! request-rolling!
                                          op-describe op-lssessions op-lsmiddleware
                                          op-eval
                                          op-ciderversion op-apropos op-docstring op-completions op-resolve-symbol op-stacktrace]]
   [demo.views]))

(defn print-partial [res]
  (warn "partial result: " res))

(defn get-data
  "demo nrepl websocket
   uses the async request api (layer 2)"
  [conn data]
  (let [; helper fn. makes nrepl request "op" and stores result with :k in data atom
        r! (fn [k op]
             (go (let [r (<! (send-request! conn op))]
                   (info k " result: " r)
                   (swap! data assoc k r))))]

    (info "making nrepl reqs")
    (request-rolling! conn {:op "eval" :code "(+ 7 7)"} print-partial)
    (request-rolling! conn {:op "sniffer-sink"} print-partial)

    ; nrepl
    (r! :01-describe (op-describe))
    (r! :02-sessions (op-lssessions))
    (r! :03-mw (op-lsmiddleware))

    ; eval
    (r! :04-eval (op-eval "(println 3)(* 7 7)(println 5)"))

    ; cider
    (r! :05-ciderv (op-ciderversion)) ; cider version fucks up other request, unsure why
    (r! :06-apropos (op-apropos "pprint"))
    (r! :07-docstring (op-docstring "doseq" "clojure.core"))
    (r! :08-complete (op-completions "ma" "user" "(def a 4)"))
    (r! :09-resolve (op-resolve-symbol "pprint" "clojure.pprint"))
    (r! :10-evalex (op-eval "(throw Exception \"b\")")) ; make eval exception
    (r! :11-stack (op-stacktrace)) ; get stacktrace after exception happened

;
    ))

(defmethod reagent-page :demo/ops [{:keys [route-params query-params handler] :as route}]
  (let [nrepl-status (rf/subscribe [:nrepl/status])
        data (r/atom {})
        first (r/atom true)]
    (fn [{:keys [route-params query-params handler] :as route}])
    (when @first
      (reset! first false)
      (get-data (:conn @nrepl-status) data))

    [demo.views/app (:conn @nrepl-status) data]))



