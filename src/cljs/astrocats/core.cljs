(ns astrocats.core
  (:require [astrocats.socket :as sockets]
            [ac-view.core :as ac-view]
            )
  )

(def game-map (atom nil))

(defn ^:export main [] 
  (let [ws (sockets/socket 
             (str (sockets/get-host-url) "echo/"))]
    ;; set open websocket handler
    (sockets/listen! ws :open (fn [e] (print "connected!")))
    ;; start view
    (ac-view/bootstrap "game")
    ))

(set! *print-fn* #(.log js/console (apply str %&))) ;;TODO remove
(set! (.-onload js/window) main)
