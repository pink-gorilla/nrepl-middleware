(defproject org.pinkgorilla/gorilla-middleware "0.2.3"
  :description "Gorilla REPL nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  ;:deploy-repositories [["releases" :clojars]]
  :repositories [["clojars" {:url           "https://clojars.org/repo"
                             :username      "pinkgorillawb"
                             :sign-releases false}]]
  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [org.clojure/data.json "0.2.6"]
   [nrepl "0.6.0"]
   [cider/cider-nrepl "0.22.4"]
   [clojail "1.0.6"]
   [cider/piggieback "0.4.2"]
   [org.pinkgorilla/gorilla-renderable "2.1.0"]]
  :repl-options {:init-ns pinkgorilla.middleware.cljs})
