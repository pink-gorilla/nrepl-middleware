(defproject pinkgorilla.ui.gorilla-middleware "0.1.7"
  :description "Gorilla REPL nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  ;:deploy-repositories [["releases" :clojars]]
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :username "pinkgorillawb"
                             :sign-releases false}]]
  :dependencies 
  [[org.clojure/clojure "1.9.0-alpha13"]
   [org.clojure/data.json "0.2.6"]
   [org.clojure/tools.nrepl "0.2.12"]

   [pinkgorilla.ui.gorilla-renderable "2.0.7"]
   
   [cider/cider-nrepl "0.14.0"]
   [clojail "1.0.6"]
   [com.cemerick/piggieback "0.2.1" :exclusions [org.clojure/clojurescript]]
   [org.clojure/clojurescript "1.9.293" :scope "provided"] ;awb99: this project does not use clojurescript. why is this here?
                 ;; [clojure.tools.logging :as log]
   ]
  :repl-options 
  {:init-ns pinkgorilla.middleware.cljs})