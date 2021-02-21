(ns pinkgorilla.nrepl.middleware.datafy
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [clojure.core.protocols]
   [clojure.datafy]
   #_[picasso.converter :refer [->picasso]]))


; https://github.com/RickMoynihan/nrebl.middleware
; https://github.com/Lokeh/punk
; https://github.com/pedro-w/nav-demo
; https://www.youtube.com/watch?v=c52QhiXsmyI&list=PLZdCLR02grLpMkEBXT22FTaJYxB92i3V3&index=3
; PREPL = Repl + presentation layer. klipse already supports this!!!


(def current (atom 0))

(defn next-id []
  (swap! current inc)
  @current)

(def items (atom {}))

; no logging in datafy-id as this gets sent to nrepl-client as it captures std-out
(defn datafy-id [x]
  (if (satisfies? clojure.core.protocols/Datafiable x)
    (let [dx (clojure.datafy/datafy x)
          idx (next-id)]
      ;(info "datafy id: " idx)
      (swap! items assoc idx dx)
      {:idx idx
       :value dx
       :meta (meta dx)})
    {:value x
     :meta (meta x)}))

(defn hack-value [idx item k]
  (info "hack-value idx" idx "item:" item  "key: " k " type: " (type item))
  (cond
    (seq? item) (nth item k)
    :else (get item k)))

(defn nav! [idx k v]
  (info "nav! idx:" idx " key:" k " val:" v)
  (let [item (get @items idx)
        v (hack-value idx item k)
        _ (info "hacked value: " v)
        x* (clojure.datafy/nav item k v)
        ;d* (clojure.datafy/datafy x*)
        ]
    #_{:idx idx
       :value d*
       :meta (meta d*)}
    (datafy-id x*)))

(comment
  (get [1 2 3] 1)
  (def d [5 6 7])
  (meta (clojure.datafy/datafy d))
  (clojure.datafy/nav (clojure.datafy/datafy d) 1 (get d 1))

  (def n (clojure.datafy/datafy *ns*))
  n
  (meta n)
  (clojure.datafy/datafy (clojure.datafy/nav n :imports Appendable))

  (def v (clojure.datafy/datafy [7 6 5]))
  v
  (meta v)

  ;
  (require '[picasso.datafy.file])
  (def p (clojure.datafy/datafy (picasso.datafy.file/make-path "/")))
  p
  (def p1 (clojure.datafy/nav p :children (get p :children)))
  (def p2 (clojure.datafy/nav (clojure.datafy/datafy p1) 3 (nth p1 3)))
  (clojure.datafy/datafy p2)


 ; 
  )

