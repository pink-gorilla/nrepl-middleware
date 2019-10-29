(defproject pinkgorilla.ui.gorilla-middleware "0.2.0"
  :description "Gorilla REPL nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  ;:deploy-repositories [["releases" :clojars]]
  :repositories [["clojars" {:url           "https://clojars.org/repo"
                             :username      "pinkgorillawb"
                             :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [nrepl "0.6.0"]
                 ;; [org.clojure/tools.nrepl "0.2.13"]
                 [pinkgorilla.ui.gorilla-renderable "2.0.12"]
                 [cider/cider-nrepl "0.22.4"]
                 [clojail "1.0.6"]
                 ;; [com.cemerick/piggieback "0.2.2"
                 [cider/piggieback "0.4.2"
                  :exclusions [org.clojure/clojurescript]]
                 [org.clojure/clojurescript "1.9.293" :scope "provided"] ;TODO: awb99: this project does not use clojurescript. why is this here?
                 ]
  :repl-options
  {:init-ns pinkgorilla.middleware.cljs})
