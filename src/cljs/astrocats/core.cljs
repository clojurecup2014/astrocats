(ns astrocats.core
  (:require [astrocats.socket :as socket]
            [ac-view.core :as ac-view]))

(set! *print-fn* #(.log js/console (apply str %&))) ; TODO remove this

(def game-map (atom nil))
(def ws (atom nil))

(defn l-listener
  []
  (socket/send! @ws {:type "cat"
                     :key "left"}))

(defn r-listener
  []
  (socket/send! @ws {:type "cat"
                     :key "right"}))

(defn z-listener
  []
  (socket/send! @ws {:type "cat"
                     :key "jump"}))

(defn ^:export main []
    (reset! ws (socket/socket
                 (str (socket/get-host-url) "echo/")))
    ;; set open websocket handler
    (socket/listen! @ws :open (fn [e] (print "connected!")))
    ;; set msg websocket handler
    (socket/listen! @ws :msg (fn [e] (print "on message: " (. e -data))
                                     (ac-view/call-event! (. e -data))))
    ;; start view
    (ac-view/bootstrap "game" l-listener r-listener z-listener))

(set! (.-onload js/window) main)
