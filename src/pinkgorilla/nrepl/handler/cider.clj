(ns pinkgorilla.nrepl.handler.cider)

#_(def middleware-cider
    "A vector containing the CIDER middleware pinkgorilla supports."
    '[cider.nrepl/wrap-version
      cider.nrepl/wrap-apropos
      cider.nrepl/wrap-complete
      cider.nrepl/wrap-info
      cider.nrepl/wrap-stacktrace])

(def middleware-cider
  "A vector containing the CIDER middleware pinkgorilla supports."
  '[cider.nrepl/wrap-apropos
    ;cider.nrepl/wrap-classpath
    ;cider.nrepl/wrap-clojuredocs
    cider.nrepl/wrap-complete
    ;cider.nrepl/wrap-content-type
    ;cider.nrepl/wrap-debug
    ;cider.nrepl/wrap-enlighten
    ;cider.nrepl/wrap-format
 ;   cider.nrepl/wrap-info    ; disabled because of exception: java.lang.ClassNotFoundException: jdk.javadoc.doclet.Doclet
    ;cider.nrepl/wrap-inspect
    ;cider.nrepl/wrap-macroexpand
    ;cider.nrepl/wrap-ns
    ;cider.nrepl/wrap-out
    ;cider.nrepl/wrap-slurp
    ;cider.nrepl/wrap-profile
    ;cider.nrepl/wrap-refresh
    ;cider.nrepl/wrap-resource
    ;cider.nrepl/wrap-spec
    cider.nrepl/wrap-stacktrace
    ;cider.nrepl/wrap-test
    ;cider.nrepl/wrap-trace
    ;cider.nrepl/wrap-tracker
    ;cider.nrepl/wrap-undef
    cider.nrepl/wrap-version
    ;cider.nrepl/wrap-xref
    ])

(defn require-cider []
  ;; force side effects at runtime in nrepl-process
  (require 'cider.nrepl))

(defn resolve-or-fail [sym]
  (or (resolve sym)
      (throw (IllegalArgumentException. (format "Cannot resolve %s" sym)))))

(defn middleware-cider-resolve []
  (map resolve-or-fail middleware-cider))