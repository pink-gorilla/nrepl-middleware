(ns clojupyter.protocol.mime-convertible
  (:require [clojupyter.util :as u]
            [io.simplect.compose :refer [redefn]]))

(defprotocol PMimeConvertible
  (to-mime [o]))

