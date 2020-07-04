(ns pinkgorilla.nrepl.sniffer.notebook
  (:require
   [clojure.string :as str]
   [picasso.default-config] ; side-effects 
   [picasso.converter :refer [->picasso]]
   [pinkgorilla.nrepl.logger :refer [log!]]
   [pinkgorilla.nrepl.sniffer.core :as sniffer]))

(defn our-msg? [msg]
  (let [msg-session-id (:session msg)
        our-session-id (sniffer/current-session-id)]
    (and our-session-id
         (= our-session-id msg-session-id))))

(defn remote-msg? [msg]
  (not (our-msg? msg)))

        ;(when ;(nil? (:out resp))  
        ; true ; (remote-msg? msg) )

(defn render-value [value]
  (let [r (->picasso value)]
    r))

(defn ->notebook [{:keys [op code] :as msg}
                  {:keys [id session ns status value out] :as resp}]
  (when (and op (= op "pinkieeval"))
    (log! {:pinkie-eval "how great is this?"
           :code code}))
  (when (and op
             (= op "eval")
             ;ns
             (or (and ns (find resp :value))
                 (and (nil? ns) out))
             (not (str/starts-with? code "(with-in-str ")) ;vscode load file to repl
             (not (str/starts-with? code "(in-ns '")) ; vs code does this before evals
             (not (symbol? value)) ; response to in-ns
             #_(:as-picasso msg))
    (let [pinkie (if value (render-value value) nil)
          eval-result {:session session
                       :id id
                       :ns ns
                       :code code
                       :value value
                       :pinkie pinkie
                       :out out}]
      #_(log! {:mval (meta value)
               :mcode (meta code)})
      #_(log! resp)
      (log! eval-result)
      eval-result)))



