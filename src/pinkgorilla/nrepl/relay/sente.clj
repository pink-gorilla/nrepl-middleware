(ns pinkgorilla.nrepl.relay.sente
    (:require
     [clojure.string]
     [taoensso.timbre :as log :refer [debug info infof warn error errorf]]
     [clojure.core.async :refer [chan go go-loop <! >!]]
     [webly.ws.core :refer [send! send-all! send-response]]
     [webly.ws.msg-handler :refer [-event-msg-handler]]
     [pinkgorilla.nrepl.client.core :refer [connect request-rolling!]]
     ;[pinkgorilla.nrepl.client.connection.in-process :refer [connect]]
    [pinkgorilla.nrepl.client.request :as r :refer [create-multiplexer!]]

     ))

(info "starting nrepl sente relay")
(def conn (connect {:type :in-process}))

(defn relay-responses [ev-msg req]
  (let [req-id (:id req)
        uid (:uid ev-msg)]
  (infof "relaying nrepl req: %s to: %s " req uid)
  (request-rolling! conn req
                    (fn [res]
                      ;(let [res (assoc res :id req-id)]
                        (infof "relaying to: %s nrepl res: " uid res)
                        (send-response ev-msg :nrepl/res res)
                      )
                    :raw
                    )))

(comment
  ; ev-msg
  {:?reply-fn nil, 
 :ch-recv #object[clojure.core.async.impl.channels.ManyToManyChannel 0x4c637b33 "clojure.core.async.impl.channels.ManyToManyChannel@4c637b33"], 
 :client-id "819c92ec-da42-417c-845c-883b63cd123e", 
 :connected-uids #atom[{:ws #{"685e0c86-120d-4c5e-adde-01773c9b9748"}, 
                        :ajax #{}, 
                        :any #{"685e0c86-120d-4c5e-adde-01773c9b9748"}} 0x3418eaf1],
 :uid "685e0c86-120d-4c5e-adde-01773c9b9748",
 :event [:nrepl/req {:op "ls-sessions", 
                     :id "478db1fb-4355-4419-999c-0f3324bfac3c"}], 
 :id :nrepl/req, 
 :send-buffers #atom[{:ws {}, :ajax {}} 0x447dde33],
 :ring-req {:ssl-client-cert nil, :protocol "HTTP/1.1", 
            :cookies {"_ga" {:value "GA1.1.370433891.1617742081"}, 
                      "ring-session" {:value "2219f49e-c81c-4177-b7a8-7b354503d9ec"}}, 
            :remote-addr "[0:0:0:0:0:0:0:1]", 
            :params {:client-id "819c92ec-da42-417c-845c-883b63cd123e", 
                     :csrf-token "w41PJu851PdW2btbETUAU3i4wYwZnBKAH2YbvF8GlwVOjc1BtP94HWO4s8+2StZVsL0gcqMnnND85hpk"}, 
            :flash nil, 
            :headers {"origin" "http://localhost:8000", "host" "localhost:8007", "upgrade" "websocket", "user-agent" "Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36", "cookie" "_ga=GA1.1.370433891.1617742081; ring-session=2219f49e-c81c-4177-b7a8-7b354503d9ec", "connection" "Upgrade", "pragma" "no-cache", "sec-websocket-key" "88IL1K8n8r35/gH/ZQWwew==", "accept-language" "de,de-AT;q=0.9,en-US;q=0.8,en;q=0.7,es-CO;q=0.6,es;q=0.5", "sec-websocket-version" "13", "accept-encoding" "gzip, deflate, br", "sec-websocket-extensions" "permessage-deflate; client_max_window_bits", "cache-control" "no-cache"}, 
            :server-port 8007, :websocket-subprotocols [], :form-params {}, :websocket-extensions [#object[org.eclipse.jetty.websocket.common.JettyExtensionConfig 0x4c65947d "permessage-deflate;client_max_window_bits"]], :session/key nil, :query-params {"client-id" "819c92ec-da42-417c-845c-883b63cd123e", "csrf-token" "w41PJu851PdW2btbETUAU3i4wYwZnBKAH2YbvF8GlwVOjc1BtP94HWO4s8+2StZVsL0gcqMnnND85hpk"}, :uri "/api/chsk", :server-name "localhost", :anti-forgery-token "pngrgein/5exC8PbU5mc5pHjEaExoCGGfUoXaGEfvJ9ychdMWuFpTSzOT3FqobuEu02gYM9nGyuaQbsv", :query-string "client-id=819c92ec-da42-417c-845c-883b63cd123e&csrf-token=w41PJu851PdW2btbETUAU3i4wYwZnBKAH2YbvF8GlwVOjc1BtP94HWO4s8%2B2StZVsL0gcqMnnND85hpk", :multipart-params {}, :scheme :http, :request-method :get, :session {}}, :?data {:op "ls-sessions", :id "478db1fb-4355-4419-999c-0f3324bfac3c"}, :send-fn #function[taoensso.sente/make-channel-socket-server!/send-fn--33424]}
;
  )

(defmethod -event-msg-handler :nrepl/req
  [{:as ev-msg :keys [event id ?data]}]
  (let [[_ req] event] ; _ is :nrepl/req
     (relay-responses ev-msg req)
    ))