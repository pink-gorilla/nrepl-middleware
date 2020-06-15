(ns pinkgorilla.nrepl.autocomplete-simple
  (:require [clojure.string :as str]))

(def special-forms
  (mapv str
        '(case* catch def defrecord* deftype* do finally fn* if js* let*
                letfn* loop* new ns quote recur set! throw try)))

(def ^:private valid-prefix #"/?([a-zA-Z0-9\-.$!?\/><*=\?_]+)")

#_(defn- normalize-results [result]
  (vec (some->> result
                helpers/parse-result
                :result
                (map (fn [c] {:type :function :candidate c})))))

(def ^:private re-char-escapes
  (->> "\\.*+|?()[]{}$^"
       set
       (map (juxt identity #(str "\\" %)))
       (into {})))

(defn- re-escape [prefix]
  (str/escape (str prefix) re-char-escapes))

(defn for-clj [repl ns-name txt-prefix]
  (let [prefix (->> txt-prefix (re-seq valid-prefix) last last)
        cmd (str "(clojure.core/let [collect #(clojure.core/map "
                 "(clojure.core/comp str first) "
                 "(%1 %2)) "
                 "refers (collect clojure.core/ns-map *ns*)"
                 "from-ns (->> (clojure.core/ns-aliases *ns*) "
                 "(clojure.core/mapcat (fn [[k v]] "
                 "(clojure.core/map #(str k \"/\" %) "
                 "(collect clojure.core/ns-publics v)))))] "
                 "(clojure.core/->> refers "
                 "(concat from-ns) "
                 "(clojure.core/filter #(re-find #\""
                 (re-escape txt-prefix) "\" %)) "
                 "(clojure.core/sort)"
                 "vec"
                 "))")]
    (if (not-empty prefix)
      (.. (eval/eval repl cmd {:namespace ns-name :ignore true})
          (then normalize-results)
          (catch (constantly [])))
      (p/promise []))))