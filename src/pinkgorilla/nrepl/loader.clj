



    (send! {:op "eval" :code "(require '[goldly.nrepl.sniffer.middleware])"})
    ;(send! {:op "eval" :code "\"goldly snippet jack-in ..\""})
    ;(send! {:op "eval" :code "(require '[pinkgorilla.ui.hiccup_renderer])"})
(send! {:op "add-middleware"
        :middleware ['goldly.nrepl.sniffer.middleware/render-values
                       ;'goldly.nrepl.middleware/wrap-pinkie
                     ]})