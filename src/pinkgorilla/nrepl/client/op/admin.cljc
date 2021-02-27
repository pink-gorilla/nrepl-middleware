(ns pinkgorilla.nrepl.client.op.admin
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn error]]
      :clj [taoensso.timbre :refer [debug info warn error]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [single-key-concat multiple-key-concat]]))

;todo:
; op: close
; op: clone


(defmethod init :describe [req]
  (multiple-key-concat [:versions :ops]))

(defmethod init :ls-sessions [req]
  (multiple-key-concat :sessions))

(defmethod init :interrupt [req]
  (multiple-key-concat :unknown))

