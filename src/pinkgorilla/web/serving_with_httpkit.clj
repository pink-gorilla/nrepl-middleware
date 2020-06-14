(ns pinkgorilla.web.serving-with-httpkit
  (:require
   [clojure.tools.logging :as log]
   [ring.util.response :as resp]
   [compojure.core :as comp]
   [de.otto.tesla.serving-with-httpkit :as tesla-httpkit]
   [de.otto.tesla.stateful.handler :as handler]
   [com.stuartsierra.component :as c])
  (:gen-class))

(defrecord HttpkitServer [config handler]
  c/Lifecycle
  (start [self]
    (log/info "-> Starting server")
    (let [handler-404 (comp/ANY "*" _request (resp/status (resp/response "") 404))
          all-handlers (comp/routes (handler/handler handler) handler-404)
          options (tesla-httpkit/server-config (:config self))
          ;server (jetty-ws/run-jetty all-handlers (merge {:port (tesla-jetty/port config)
          ;                                                :join?  false
          ;                                                 }
          ;                                               options))
          server (tesla-httpkit/run-server all-handlers  
                                           (merge {:port 9000 ; (tesla-jetty/port config)
                                                                 :join?  false}
                                                                options))
         ]
      (log/info "Options" options)
      (assoc self :jetty server)))
  (stop [self]
    (log/info "<- Stopping server")
    (if-let [server (:jetty self)]
      (.stop server))
    self))

(defn new-server [] (map->HttpkitServer {}))

(defn add-jetty-server
  [base-system & server-dependencies]
  (assoc base-system :server (c/using (new-server) (reduce conj [:config :handler] server-dependencies))))
