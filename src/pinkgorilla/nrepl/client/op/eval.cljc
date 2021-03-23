(ns pinkgorilla.nrepl.client.op.eval
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [tracef debug debugf info infof warn error errorf]]
      :clj [taoensso.timbre         :refer [tracef debug debugf info infof warn error errorf]])
   #?(:cljs [cljs.reader :refer [read-string]]
      :clj [clojure.core :refer [read-string]])
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

;#?(:clj
;   (def Error {}))

#?(:cljs
   (defn- picasso-unwrap
     "picasso middleware serializes picasso values to edn.
   It does this becaus nrepl might usesay bencode for its connection.
   In this case we would loose meta-data, Therefor picasso values ae sent as string
   on the wire.
   In case nrepl transport is edn (which new versions do), then
   we have edn within edn :-(.
   "
     [value]
     (try
       (tracef "picasso-unwrap %s" value)
       (when value (read-string value))
       (catch js/Error e
         (error "picasso-unwrap parsing %s ex: %s" value e))))

   :clj
   (defn- picasso-unwrap
     "picasso middleware serializes picasso values to edn.
   It does this becaus nrepl might usesay bencode for its connection.
   In this case we would loose meta-data, Therefor picasso values ae sent as string
   on the wire.
   In case nrepl transport is edn (which new versions do), then
   we have edn within edn :-(.
   "
     [value]
     (try
       (tracef "picasso-unwrap %s" value)
       (when value (read-string value))
       (catch Exception e
         (errorf "picasso-unwrap parsing %s ex: %s" value (.getMessage e)))))
   ;
   )

; used also by sniffer in notebook
(defn process-fragment
  "result is an atom, containing the eval result.
   processes a fragment-response and modifies result-atom accordingly."
  [result {:keys [out err root-ex ns value picasso datafy]}]
  (-> result
      ; console 
      (cond-> out (assoc :out (str (:out result) out)))

      ; eval error
      (cond-> err (assoc :err err))

      ; datafy
      (cond-> datafy (assoc :datafy datafy))

      ; value /namespace
      (cond-> ns (assoc :ns ns
                        :value (conj (:value result) value)
                        :picasso (conj (:picasso result) (picasso-unwrap picasso))))

      ; root exception ?? what is this ?? where does it come from ? cider? nrepl?
      (cond-> root-ex (assoc :root-ex root-ex))))


; used also by sniffer in notebook
(def initial-value {:value []
                    :picasso []
                    :ns nil
                    :out ""
                    :err []
                    :root-ex nil})

(defmethod init :eval [req]
  {:initial-value initial-value
   :process-fragment process-fragment})
