(ns pinkgorilla.nrepl.aot
  (:require
   [pinkgorilla.nrepl.server.nrepl-server]
   [pinkgorilla.nrepl.server.add-middleware]
   [pinkgorilla.nrepl.relay.jetty]
   ;[cider.nrepl]
   ; aot cider does fuckup nrepl.
   ;[WARNING] No nREPL middleware descriptor in metadata of #'cider.piggieback/wrap-cljs-repl, see nrepl.middleware/set-descriptor!
   ))
; used in aot.sh