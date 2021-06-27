(ns pinkgorilla.nrepl.data.demo-ops)

(def op-eval-simpel
  [{:op "eval" :code "(+ 7 7)"}
   {:op "eval" :code "2"}
   {:op "eval" :code "3"}])

(def op-eval-sniffer
  [{:op "sniffer-source"}
   {:op "eval"  :code "(+ 1 1)"}
   {:op "eval"  :code ":gorilla/on"}
   {:op "eval"  :code "(+ 2 2)"}
   {:op "info" :ns "demo.data" :symbol ":gorilla/on"}
   {:op "eval"  :code "(+ 3 3)"}])

(def op-eval-sniffer-picasso
  [{:op "eval" :code ":gorilla/on"}

   {:op "eval"  :code "^:X (+ 2 2)"}
   {:op "eval"  :code "^:R [:p/vega (+ 8 8)]"}
   {:op "eval"  :code "^:U (time (reduce + (range 1e6)))"}

   {:op "eval"  :code ":gorilla/off"}
   {:op "eval"  :code "\"NO\""}
   {:op "eval"  :code ":gorilla/on"}
   {:op "eval"  :code "\"YES\""}

  ; evals inside notebook would have this flag. check if it works:
   {:op "eval" :as-picasso 1 :code "^:R [:p (+ 8 8)]"}

   {:op "eval"  :code "(+ 1 1) (+ 2 2) (+ 3 3)"}

   {:op "eval" :code "(time (reduce + (range 1e6)))"}])
(def op-nrepl
  [{:op "describe"}
   {:op "ls-sessions"}
   {:op "ls-middleware"}
   {:op "interrupt"}])

(def op-cider
  [{:op "cider-version"}
   {:op "complete-doc" :symbol "doseq" :ns "clojure.core"}
   {:op "apropos" :query "pprint"}
   {:op "complete" :symbol "ma" :ns "user" :context "(def a 4)"}
   {:op "info" :ns "clojure.pprint" :symbol "pprint"} ; resolve symbol
   {:op "stacktrace"}])

(def op-eval-ex
  [{:op "eval" :as-picasso 1 :code "(throw (Exception. \"my exception message\"))"}
   {:op "eval" :as-picasso 1 :code "(+ 1"} ; missing closing bracket
   ])
(def op-gorilla
  [{:op "sniffer-status"}
   {:op "sniffer-sink"}])


