(ns demo
  (:require
   [orchard.java.classpath :as o]
  ; [orchard.info :as i]
   )
  (:import
   (jdk.javadoc.doclet Doclet DocletEnvironment))
  (:gen-class))

(defn -main [& args]
  (println "scp: " (o/system-classpath))
  (println "cp: " (o/classpath))
 ; (println "info:" (o/info 'demo 'willy))
  
  )

