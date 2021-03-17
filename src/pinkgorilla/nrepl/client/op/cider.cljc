(ns pinkgorilla.nrepl.client.op.cider
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn error]]
      :clj [taoensso.timbre :refer [debug info warn error]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [single-key-concat multiple-key-concat]]))

(defmethod init :cider-version [req]
  (multiple-key-concat [:cider-version]))

(defmethod init :apropos [req]
  (multiple-key-concat [:apropos-matches]))

(defmethod init :complete [req]
  (single-key-concat :completions))

(defmethod init :complete-doc [req]
  (single-key-concat :completion-doc))

(defmethod init :info [req]
  (multiple-key-concat ["name" "ns"
                        "resource" "added"
                        "file" "line" "column"
                        "see-also" "arglists-str" "doc"]))

#_[{"resource" "clojure/pprint/pprint_base.clj",
    "name" "pprint",
    "added" "1.2",
    :status #{:done},
    "line" 241,
    "column" 1,
    "file" "jar:file:/home/andreas/.m2/repository/org/clojure/clojure/1.10.1/clojure-1.10.1.jar!/clojure/pprint/pprint_base.clj",
    "see-also" ("clojure.pprint/pp" "clojure.pprint/print-table" "clojure.core/prn" "clojure.core/prn-str" "clojure.core/pr" "clojure.core/pr-str" "clojure.pprint/pprint-tab"),
    "arglists-str" "[object]\n[object writer]",
    "doc" "Pretty print object to the optional output writer. If the writer is not provided, \nprint the object to the currently bound value of *out*.",
    "ns" "clojure.pprint"}]

(defmethod init :stacktrace [req]
  (single-key-concat :stacktrace))

