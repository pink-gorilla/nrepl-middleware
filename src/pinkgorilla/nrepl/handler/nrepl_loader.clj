(ns pinkgorilla.nrepl.handler.nrepl-loader)

; nrepl docs:
; https://nrepl.org/nrepl/ops.html#_add_middleware

(def ops-sniffer
  [{:op "describe"}
   {:op "eval" :code "(require '[pinkgorilla.nrepl.middleware.sniffer])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ["pinkgorilla.nrepl.middleware.sniffer/wrap-sniffer"]}
   {:op "ls-middleware"}
   ;{:op "close"} ; this needs to have param of the session to be closed
]) ; 

(def ops-picasso
  [{:op "eval" :code "(require '[pinkgorilla.nrepl.middleware.picasso])"}
   {:op "eval" :code "(require '[picasso.default-config])"} ; for side effects
   {:op "add-middleware"
    :middleware ['pinkgorilla.nrepl.middleware.picasso/wrap-picasso]}])