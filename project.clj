(defproject org.pinkgorilla/gorilla-middleware "0.2.25-SNAPSHOT"
  :description "Pink Gorilla nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/release_username
                                     :password :env/release_password
                                     :sign-releases false}]]
  :min-lein-version "2.9.3"
  :min-java-version "1.11"


  :source-paths ["src"]
  :target-path  "target/jar"
  :clean-targets ^{:protect false} [:target-path
                                    [:demo :builds :app :compiler :output-dir]
                                    [:demo :builds :app :compiler :output-to]]

  :uberjar-exclusions [#"cider/nrepl.*\.class$"]

  :managed-dependencies [[org.clojure/clojure "1.10.1"]
                         [org.clojure/core.async "1.2.603"]
                         [org.clojure/clojurescript "1.10.773"]
                         [com.cognitect/transit-clj "1.0.324"]
                         [com.cognitect/transit-cljs "0.8.264"]
                         [com.fasterxml.jackson.core/jackson-core "2.11.0"]
                         [cheshire "5.10.0"]
                         [org.clojure/tools.reader "1.3.2"]]


  :dependencies  [;[org.clojure/clojure "1.10.1"]
                  ;[org.clojure/spec.alpha "0.2.187"]
                  ;[org.clojure/data.json "0.2.6"]
                  ;[com.stuartsierra/component "0.4.0"]
                  [jarohen/chord "0.8.1" ; nrepl websocket
                   :exclusions [com.cognitect/transit-clj
                                com.cognitect/transit-cljs]] ; websockets with core.async
                  [nrepl "0.7.0"]
                  [cider/cider-nrepl "0.22.4"]
                  [cider/piggieback "0.4.2"]
                  [clojail "1.0.6"] ; sandboxing
                  [org.pinkgorilla/gorilla-renderable "3.0.15"]]

  :profiles {:cljs {:repl-options   {:init-ns          demo.core
                                     :port             4001
                                     :nrepl-middleware [shadow.cljs.devtools.server.nrepl/middleware]}

                    :dependencies [[org.clojure/core.async "1.2.603"]
                                   [org.clojure/clojurescript "1.10.773"]
                                   [org.clojure/tools.analyzer "1.0.0"]

                                   ; shadow-cljs MAY NOT be a dependency in lein deps :tree -> if so, bundeler will fail because shadow contains core.async which is not compatible with self hosted clojurescript
                                   [thheller/shadow-cljs "2.10.7"]
                                   [thheller/shadow-cljsjs "0.0.21"]
                                   [com.taoensso/timbre "4.10.0"]             ; clojurescript logging
                                   [com.lucasbradstreet/cljs-uuid-utils "1.0.2"] ;; awb99: in encoding, and clj/cljs proof
                                   [reagent "0.10.0"
                                    :exclusions [org.clojure/tools.reader
                                                 cljsjs/react
                                                 cljsjs/react-dom]]
                                   
                                   ]}

             :dev   {:dependencies [[org.clojure/tools.logging "1.0.0"]
                                    [com.stuartsierra/component "0.4.0"]
                                    [com.taoensso/timbre "4.10.0"]             ; clojurescript logging
                                    [ring "1.7.1"]
                                    [ring-cors "0.1.13"]
                                    [ring/ring-defaults "0.3.2"
                                     :exclusions [javax.servlet/servlet-api]]
                                    ;[javax.websocket/javax.websocket-api "1.1"]
                                    ;[javax.servlet/javax.servlet-api "4.0.1"]
                                    [compojure "1.6.1"] ; Routing
                                    ;[org.eclipse.jetty.websocket/websocket-server "9.4.12.v20180830"]
                                    [info.sunng/ring-jetty9-adapter "0.12.5"]

                                   ; [de.otto/tesla-jetty "0.2.6"
                                     ;:exclusions [org.eclipse.jetty/jetty-server
                                      ;            org.eclipse.jetty/jetty-servlet]
                                    ; ]

                                    [clj-kondo "2020.06.12"]]
                     :plugins [[lein-cljfmt "0.6.6"]
                               [lein-cloverage "1.1.2"]
                               [lein-shell "0.5.0"]
                               [lein-codox "0.10.7"]
                               [lein-ancient "0.6.15"]
                               [min-java-version "0.1.0"]]

                     :aliases {"clj-kondo"
                               ["run" "-m" "clj-kondo.main"]}

                     :cloverage {:codecov? true
                                 ;; In case we want to exclude stuff
                                 ;; :ns-exclude-regex [#".*util.instrument"]
                                 ;; :test-ns-regex [#"^((?!debug-integration-test).)*$$"]
                                 }
                     ;; TODO : Make cljfmt really nice : https://devhub.io/repos/bbatsov-cljfmt
                     :cljfmt  {:indents {as->                [[:inner 0]]
                                         with-debug-bindings [[:inner 0]]
                                         merge-meta          [[:inner 0]]
                                         try-if-let          [[:block 1]]}}}}

  :aliases {"bump-version"
            ["change" "version" "leiningen.release/bump-version"]

            "build-shadow-ci"
            ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "compile" ":demo"] ; :ci

            ;"shadow-watch-demo" ["run" "-m" "shadow.cljs.devtools.cli" "watch" ":demo"]

            "build-test"  ^{:doc "Builds Bundle. Gets executed automatically before unit tests."}
            ["with-profile" "+test" "shell" "shadow-cljs" "compile" "ci"]

            "test-run" ^{:doc "Runs unit tests. Does not build the bundle first.."}
            ["shell" "./node_modules/karma/bin/karma" "start" "--single-run"]

            "test-js" ^{:doc "Run Unit Tests. Will compile bundle first."}
            ["do" "build-test" ["test-run"]]

            "demo"  ^{:doc "Runs demo  via webserver."}
            ["shell" "shadow-cljs" "watch" "demo"]
            
            "relay"
               ["run" "-m" "demo.core"]
            }

  :release-tasks [["vcs" "assert-committed"]
                  ["bump-version" "release"]
                  ["vcs" "commit" "Release %s"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["bump-version"]
                  ["vcs" "commit" "Begin %s"]
                  ["vcs" "push"]]

  ;:repl-options {:init-ns pinkgorilla.middleware.cljs}
  )
