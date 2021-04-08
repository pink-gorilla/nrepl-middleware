(defproject org.pinkgorilla/nrepl-middleware "0.3.28-SNAPSHOT"
  :description "nREPL middleware"
  :url "https://github.com/pink-gorilla/nrepl-middleware"
  :license {:name "MIT"}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/release_username
                                     :password :env/release_password
                                     :sign-releases false}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["bump-version" "release"]
                  ["vcs" "commit" "Release %s"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["bump-version"]
                  ["vcs" "commit" "Begin %s"]
                  ["vcs" "push"]]

  :min-lein-version "2.9.3"
  :min-java-version "1.11"
  ;:jvm-opts ["-Dtrust_all_cert=true"
  ;           "-Djavax.net.ssl.trustStore=/home/andreas/.keystore"
  ;           ]

  :source-paths ["src"]
  :target-path  "target/jar"
  :clean-targets ^{:protect false} [:target-path
                                    [:demo :builds :app :compiler :output-dir]
                                    [:demo :builds :app :compiler :output-to]]

  :uberjar-exclusions [#"cider/nrepl.*\.class$"]

  :managed-dependencies [[org.clojure/clojure "1.10.1"]
                         [org.clojure/core.async "1.3.610"]
                         [org.clojure/clojurescript "1.10.773"]
                         [com.cognitect/transit-clj "1.0.324"]
                         [com.cognitect/transit-cljs "0.8.264"]
                         [com.fasterxml.jackson.core/jackson-core "2.11.2"]
                         [cheshire "5.10.0"]
                         [org.clojure/tools.reader "1.3.2"]
                         [com.google.code.findbugs/jsr305 "3.0.2"]]


  :dependencies  [;[org.clojure/clojure "1.10.1"]
                  ;[org.clojure/spec.alpha "0.2.187"]
                  ;[org.clojure/data.json "0.2.6"]
                  [org.clojure/core.async "1.3.610"]

                  ; nrepl/kernel
                  [nrepl "0.8.3"]  ; 0.7.0 lacks add-middleware
                  [cider/cider-nrepl "0.25.8"]
                  [cider/piggieback "0.5.0"]
                  [clojail "1.0.6"] ; sandboxing
                  [compliment "0.3.10"] ; code completion
                  [org.pinkgorilla/picasso "3.1.18"] ; render values

                  ; clojurescript
                  [jarohen/chord "0.8.1" ; nrepl websocket
                   :exclusions [com.cognitect/transit-clj
                                com.cognitect/transit-cljs]] ; websockets with core.async                                   
                  [com.taoensso/timbre "5.1.2"]             ; clj / cljs logging
                  [com.lucasbradstreet/cljs-uuid-utils "1.0.2"] ;; awb99: in encoding, and clj/cljs proof
                  [clj-commons/pomegranate "1.2.0"] ; add-dependency in clj kernel; TODO - Replace pomegranate with tools alpha
                  ]

  :profiles {:client {:source-paths ["profiles/client/src"]
                      :dependencies [[org.clojure/tools.cli "1.0.194"] ; commandline args
                                     ]}

             :cljs {:source-paths ["profiles/demo/src"]
                    #_:repl-options   #_{:init-ns          demo.app
                                         :port             4001
                                         :nrepl-middleware [shadow.cljs.devtools.server.nrepl/middleware]}
                    :dependencies [[org.clojure/clojurescript "1.10.773"]
                                   [org.clojure/tools.analyzer "1.0.0"]

                                   ; shadow-cljs MAY NOT be a dependency in lein deps :tree -> if so, bundeler will fail because shadow contains core.async which is not compatible with self hosted clojurescript
                                   [thheller/shadow-cljs "2.10.13"]
                                   [thheller/shadow-cljsjs "0.0.21"]
                                   [reagent "0.10.0"
                                    :exclusions [org.clojure/tools.reader
                                                 cljsjs/react
                                                 cljsjs/react-dom]]]}

             :relay-jetty {:source-paths ["profiles/demo/src"]
                           :dependencies [[ring "1.8.1"]
                                          [ring-cors "0.1.13"]
                                          [ring/ring-defaults "0.3.2"
                                           :exclusions [javax.servlet/servlet-api]]
                                    ;[javax.websocket/javax.websocket-api "1.1"]
                                    ;[javax.servlet/javax.servlet-api "4.0.1"]
                                    ;[org.eclipse.jetty.websocket/websocket-server "9.4.12.v20180830"]
                                          [info.sunng/ring-jetty9-adapter "0.14.0"]]}

             :dev   {:dependencies [[org.clojure/tools.logging "1.1.0"]
                                    [clj-kondo "2020.07.29"]]
                     :plugins [[lein-cljfmt "0.6.6"]
                               [lein-cloverage "1.1.2"]
                               [lein-shell "0.5.0"]
                               ;[lein-codox "0.10.7"] ; docs
                               [lein-ancient "0.6.15"]
                               [min-java-version "0.1.0"]]

                     :aliases {"clj-kondo"
                               ["run" "-m" "clj-kondo.main"]}

                     :cloverage {:codecov? false
                                 ;; In case we want to exclude stuff
                                 :ns-exclude-regex [#".*relay"
                                                    #"pinkgorilla.nrepl.ws.*"
                                                    #"pinkgorilla.nrepl.service.*"]
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

            "lint" ^{:doc "Runs code linter"}
            ["clj-kondo" "--lint" "src"]

            ;"shadow-watch-demo" ["run" "-m" "shadow.cljs.devtools.cli" "watch" ":demo"]

            "build-test"  ^{:doc "Builds Bundle. Gets executed automatically before unit tests."}
            ["with-profile" "+test" "run" "-m" "shadow.cljs.devtools.cli" "compile" "ci"]

            "test-run" ^{:doc "Runs unit tests. Does not build the bundle first.."}
            ["shell" "./node_modules/karma/bin/karma" "start" "--single-run"]

            "test-js" ^{:doc "Run Unit Tests. Will compile bundle first."}
            ["do" "build-test" ["test-run"]]

            "demo"  ^{:doc "Runs demo  via webserver."}
            ["with-profile" "cljs" "run" "-m" "shadow.cljs.devtools.cli" "watch" "demo"]

            "relay-jetty"
            ["with-profile" "+relay-jetty" "run" "-m" "demo.relay-jetty"]

            "client"
            ["with-profile" "+client" "run" "-m" "client.app"]}



  ;:repl-options {:init-ns pinkgorilla.middleware.cljs}
  )
