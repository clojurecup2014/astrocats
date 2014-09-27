(ns astrocats.core
  (:require [astrocats.socket :as socket]
            [ac-view.core :as ac-view]
            )
  )

(def game-map (atom nil))
(def ws (atom nil))

(defn l-listener
  []
  (print "l")
  (socket/send! @ws {:type "cat"
                     :key "left"}))

(defn r-listener
  []
  (print "r")
  (socket/send! @ws {:type "cat"
                     :key "right"}))

(defn z-listener
  []
  (print "z")
  (socket/send! @ws {:type "cat"
                     :key "jump"}))

(defn ^:export main [] 
    (reset! ws (socket/socket 
                 (str (socket/get-host-url) "echo/")))
    ;; set open websocket handler
    (socket/listen! @ws :open (fn [e] (print "connected!")))
    ;; set msg websocket handler
    (socket/listen! @ws :msg (fn [e] (print "on message: " (. e -data))))
    ;; start view
    (ac-view/bootstrap "game" l-listener r-listener z-listener))

(set! *print-fn* #(.log js/console (apply str %&))) ;;TODO remove
(set! (.-onload js/window) main)
