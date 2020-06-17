(ns pinkgorilla.nrepl.middleware.loader)

(def ops-sniffer
  [{:op "eval" :code "(require '[goldly.nrepl.sniffer.middleware])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ['goldly.nrepl.middleware/wrap-pinkie]}])

(def ops-relay
  [{:op "eval" :code "(require 'pinkgorilla.nrepl.middleware/render-values])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ['pinkgorilla.nrepl.middleware/render-values]}])