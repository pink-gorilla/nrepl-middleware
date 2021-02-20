(ns pinkgorilla.nrepl.handler.nrepl-loader)

(def ops-sniffer
  [{:op "describe"}
   {:op "eval" :code "(require '[pinkgorilla.nrepl.middleware.sniffer])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ["pinkgorilla.nrepl.middleware.sniffer/render-values-sniffer"]}
   {:op "ls-middleware"}
   ]) ; 

(def ops-relay
  [{:op "eval" :code "(require 'pinkgorilla.nrepl.middleware/render-values])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ['pinkgorilla.nrepl.middleware/render-values]}])