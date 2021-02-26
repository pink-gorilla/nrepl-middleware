# NREPL Middleware [![GitHub Actions status |pink-gorilla/gorilla-middleware](https://github.com/pink-gorilla/nrepl-middleware/workflows/CI/badge.svg)](https://github.com/pink-gorilla/nrepl-middleware/actions?workflow=CI)[![Codecov Project](https://codecov.io/gh/pink-gorilla/nrepl-middleware/branch/master/graph/badge.svg)](https://codecov.io/gh/pink-gorilla/nrepl-middleware)[![Clojars Project](https://img.shields.io/clojars/v/org.pinkgorilla/nrepl-middleware.svg)](https://clojars.org/org.pinkgorilla/nrepl-middleware)

- This project is used in [Notebook](https://github.com/pink-gorilla/gorilla-notebook) and [Goldly](https://github.com/pink-gorilla/goldly).

## features
- nrepl websocket relay (browsers can not connect to normal tcp ports)
- nrepl middleware that renders evalued results from notebook session
- nepl middlewre that sniffs evals on other nrepl session
- nrepl client (clj and cljs)on core.async

## Middleware cli demo

To test picasso and sniffer middleware run 3 different terminal windows
and execute this 3 commands in this order

```
lein relay-jetty  ; this runs jetty http server with websocket relay (port 9000)
lein client sink   ; will listenn to sniffed evals
lein client ide   ; will do a few evals that wil show up on listen
```

## WebRelay websocket demo

To see a simple websocket frontend, run in 2 terminal windows:

```
lein relay-jetty  ; this runs jetty http server with websocket relay (port 9000)
```

```
npm install
lein demo         ; browser app served with shadow-cljs dev server port 8000
```



## Design of websocket relay

Server
- Browser can only do websocket and not normal sockets.
- Server Relay runs as ring request-handler, and returns new websocket handler
- the websocket handler creates a new nrel client connection, and then
  forwards requests in both ways.
- currently we create nrepl server to connect to.

Browser:
- The code is spit into several stateful components.
- layer1 is websocket connection. Provides two core async channels.
  Manages connection / reconnection.
- layer2: request router. Manages pending requests, returns for each nrepl-op
  a dedicated return channel, which is sent fragment by fragment. 
  The request channel will be closed after the last fragment is received. 
- layer3:    

Logging:
- Lots of logging with timbre at DEBUG level
- If you dot want lots of output, set loglevel to INFO


# TODO:

## nrepl-ws-relay close session
- close of nrepl sessions:
- if client drops, a timeout needs to detect a closed session
  this is not implemented yet,
- to close a nrepl session, the session id needs to be known. 
  this means we have to read all messages that get forwarded,
  because otherwise an {:op "clone"} could reset it.
  
## nrepl-ws-relay remote nrepl
- connect to remote nrepl without client middleware setup?
- add-middleware loading works. ("add-middleware")
- but in which scenario does this make sense?

## sandboxed interruptible
-clojail: refactor or out.
