(ns pinkgorilla.nrepl.client.op.admin
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [multiple-key-concat]]))

;todo:
; op: close
; op: clone


(defmethod init :describe [req]
  (multiple-key-concat [:versions :ops]))

(defmethod init :ls-sessions [req]
  (multiple-key-concat [:sessions]))

(defmethod init :ls-middleware [req]
  (multiple-key-concat [:middleware]))

(defmethod init :interrupt [req]
  (multiple-key-concat :unknown))

