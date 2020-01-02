(defproject org.pinkgorilla/gorilla-middleware "0.2.7"
  :description "Pink Gorilla nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_username
                                     :password :env/clojars_password
                                     :sign-releases false}]]
  :dependencies  [[org.clojure/clojure "1.10.1"]
                  [org.clojure/data.json "0.2.6"]
                  [nrepl "0.6.0"]
                  [cider/cider-nrepl "0.22.4"]
                  [clojail "1.0.6"] ; sandboxing
                  [cider/piggieback "0.4.2"]
                  [org.pinkgorilla/gorilla-renderable "2.1.0"]]

  :profiles {:dev   {:dependencies [[clj-kondo "2019.11.23"]]
                     :plugins [[lein-cljfmt "0.6.6"]
                               [lein-cloverage "1.1.2"]]
                     :aliases {"clj-kondo" ["run" "-m" "clj-kondo.main"]}
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

  :aliases {"bump-version" ["change" "version" "leiningen.release/bump-version"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["bump-version" "release"]
                  ["vcs" "commit" "Release %s"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["bump-version"]
                  ["vcs" "commit" "Begin %s"]
                  ["vcs" "push"]]

  :repl-options {:init-ns pinkgorilla.middleware.cljs})
