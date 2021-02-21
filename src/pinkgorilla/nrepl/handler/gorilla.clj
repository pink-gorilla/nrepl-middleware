(ns pinkgorilla.nrepl.handler.gorilla)

(def middleware-gorilla
  '[pinkgorilla.nrepl.middleware.picasso/wrap-picasso
    pinkgorilla.nrepl.middleware.sniffer/wrap-sniffer])

(defn require-gorilla []
  ;; force side effects at runtime in nrepl-process
  (require 'picasso.default-config)
  (require 'pinkgorilla.notebook.repl)
  (require 'nrepl.middleware.print)
  (require 'picasso.datafy.file)
  (require 'pinkgorilla.nrepl.middleware.picasso)
  (require 'pinkgorilla.nrepl.middleware.sniffer))


