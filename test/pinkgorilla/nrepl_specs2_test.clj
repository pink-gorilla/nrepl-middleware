(ns pinkgorilla.nrepl-specs2-test
  (:require
   [clojure.test :refer [#_testing is deftest]]
   ;[clojure.spec.alpha :as s]
   ; bring the specs into scope:
   [pinkgorilla.kernel.nrepl-specs]))

(def msg-eval-error
  {:err
   "Error printing return value (StackOverflowError) at clojure.lang.PersistentHashMap/hash (PersistentHashMap.java:120).\nnull\n"
   :id "5ab8b4d2-1479-4e47-b1f4-365eb9cdee83"
   :session "ba744190-0cd8-4f5c-a2c9-4746123a9505"})

(def msg-eval-ex
  {:ex "class clojure.lang.ExceptionInfo"
   :id "5ab8b4d2-1479-4e47-b1f4-365eb9cdee83"
   :root-ex "class java.lang.StackOverflowError"
   :session "ba744190-0cd8-4f5c-a2c9-4746123a9505"
   :status ["eval-error"]})

;(when (s/valid? :nrepl-msg/stacktrace-msg message)
;  (info "rcvd valid stacktrace: " message))
;  
(deftest nrepl-specs-test
  (is (= true true ; (s/valid? :nrepl-msg/stacktrace-msg stacktrace-msg)
         )))


