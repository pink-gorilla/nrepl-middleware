(defproject org.clojars.deas/gorilla-middleware "0.1.1"
  :description "Gorilla REPL nREPL middleware"
  :url "https://github.com/deas/gorilla-middleware"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojars.deas/gorilla-renderable "2.1.0"]
                 [cider/cider-nrepl "0.14.0"]
                 [clojail "1.0.6"]
                 ;; [clojure.tools.logging :as log]
                 ]
  :repl-options {:init-ns gorilla-middleware.render_values-test})