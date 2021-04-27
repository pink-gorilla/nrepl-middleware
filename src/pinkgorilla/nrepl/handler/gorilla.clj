(ns pinkgorilla.nrepl.handler.gorilla)

(def middleware-gorilla
  '[pinkgorilla.nrepl.middleware.picasso/wrap-picasso
    pinkgorilla.nrepl.middleware.sniffer/wrap-sniffer])

(defn require-gorilla []
  ;; force side effects at runtime in nrepl-process
  (require 'nrepl.middleware.print)
  ;picasso
  (require 'picasso.default-config)
  (require 'pinkgorilla.notebook.repl)
  (require 'picasso.datafy.file)
  ; nrepl-miiddleware
  (require 'pinkgorilla.nrepl.middleware.picasso)
  (require 'pinkgorilla.nrepl.middleware.sniffer))


