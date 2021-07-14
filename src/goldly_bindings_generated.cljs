; generated by goldly on 2021-07-14 04:36:14
(ns goldly-bindings-generated
  (:require [webly.build.lazy :refer-macros [wrap-lazy]]
            [pinkgorilla.nrepl.kernel.kernel]
            [pinkgorilla.nrepl.client.connection.sente]
            [clojure.walk]
            [goldly.service.core]
            [goldly-server.helper.ui]
            [goldly.page.page]
            [picasso.repl]
            [pinkie.default-setup]
            [pinkie.pinkie]
            [pinkie.error]
            [pinkie.text]
            [pinkie.throw-exception]
            [pinkgorilla.repl.cljs.core]
            [tick.alpha.api]
            [thi.ng.strf.core]
            [ui.site.template]
            [ui.site.ipsum]
            [ui.site.layout]))
(def bindings-generated
  {'throw-ex pinkie.throw-exception/exception-component,
   'parse-float pinkgorilla.repl.cljs.js/parse-float,
   'error pinkgorilla.repl.cljs.log/error,
   'R! picasso.repl/R!,
   'println println,
   'timeout pinkgorilla.repl.cljs.js/timeout,
   'pm-reagent (wrap-lazy
                ui.markdown.prosemirror/prosemirror-reagent2),
   'run-a goldly.service.core/run-a,
   'prosemirror (wrap-lazy
                 ui.markdown.goldly.prosemirror-atom/prosemirror-atom),
   'error-boundary pinkie.error/error-boundary,
   'interval pinkgorilla.repl.cljs.js/interval,
   'set-system-state goldly.broadcast.core/set-system-state,
   'format goog.string/format,
   'sin pinkgorilla.repl.cljs.js/sin,
   'get-edn pinkgorilla.repl.cljs.http/get-edn,
   'app-db pinkgorilla.repl.cljs.reframe/app-db,
   'warn pinkgorilla.repl.cljs.log/warn,
   'get-str pinkgorilla.repl.cljs.http/get-str,
   'clipboard-set pinkgorilla.repl.clipboard/clipboard-set,
   'notify pinkgorilla.repl.cljs.webly/notify,
   'code (wrap-lazy
          pinkgorilla.highlightjs.viewer/code-viewer),
   'dialog pinkgorilla.repl.cljs.webly/dialog,
   'codemirror-themed2 (wrap-lazy
                        ui.code.goldly.codemirror-themed/codemirror-themed2),
   'codemirror (wrap-lazy
                ui.code.goldly.codemirror-atom/codemirror-atom),
   'text pinkie.text/text,
   'current-route pinkgorilla.repl.cljs.webly/current-route,
   'evt-val pinkgorilla.repl.cljs.js/evt-val,
   'clipboard-pop pinkgorilla.repl.clipboard/clipboard-pop,
   'reagent-page webly.web.handler/reagent-page,
   'run goldly.service.core/run,
   'codemirror-viewonly (wrap-lazy
                         ui.code.goldly.codemirror-atom/codemirror-atom-viewonly),
   'alert pinkgorilla.repl.cljs.js/alert,
   'add-page goldly.page.page/add-page,
   'markdown (wrap-lazy ui.markdown.viewer/markdown-viewer),
   'get-json pinkgorilla.repl.cljs.http/get-json,
   'info pinkgorilla.repl.cljs.log/info,
   'nav pinkgorilla.repl.cljs.webly/nav})
(def ns-generated
  {'link {'dispatch goldly-server.helper.ui/link-dispatch,
          'href goldly-server.helper.ui/link-href},
   'walk {'postwalk clojure.walk/postwalk,
          'prewalk clojure.walk/prewalk,
          'keywordize-keys clojure.walk/keywordize-keys,
          'walk clojure.walk/walk,
          'postwalk-replace clojure.walk/postwalk-replace,
          'prewalk-replace clojure.walk/prewalk-replace,
          'stringify-keys clojure.walk/stringify-keys},
   'pinkie {'register-tag pinkie.pinkie/register-tag,
            'tags pinkie.pinkie/tags,
            'render pinkie.pinkie-render/pinkie-render},
   'r {'atom reagent.core/atom},
   'rf {'dispatch re-frame.core/dispatch,
        'subscribe re-frame.core/subscribe},
   't {'now tick.alpha.api/now, 'time tick.alpha.api/time},
   'f {'format thi.ng.strf.core/format,
       'float thi.ng.strf.core/float},
   'site {'people ui.site.template/people,
          'message-button ui.site.template/message-button,
          'ipsum ui.site.ipsum/ipsum,
          'bullet-points ui.site.template/bullet-points,
          'main-with-header ui.site.layout/main-with-header,
          'foto-right ui.site.template/foto-right,
          'fotos-with-text ui.site.template/fotos-with-text,
          'foto-left ui.site.template/foto-left,
          'foto-bottom ui.site.template/foto-bottom,
          'sidebar-layout ui.site.layout/sidebar-layout}})
