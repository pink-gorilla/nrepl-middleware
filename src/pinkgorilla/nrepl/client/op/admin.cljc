(ns pinkgorilla.nrepl.client.op.admin
  (:require
   [pinkgorilla.nrepl.client.protocols :refer [init]]
   [pinkgorilla.nrepl.client.op.concat :refer [key-concat]]))

;todo:
; op: close
; op: clone


(defmethod init :describe [req]
  (key-concat [:versions :ops]))

(defmethod init :ls-sessions [req]
  (key-concat [:sessions]))

(defmethod init :ls-middleware [req]
  (key-concat [:middleware]))

(defmethod init :interrupt [req]
  (key-concat :unknown))

