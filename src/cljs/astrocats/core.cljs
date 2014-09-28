(ns astrocats.core
  (:require [astrocats.socket :as socket]
            [ac-view.core :as ac-view]))

(defn now
  []
  (.getTime (new js/Date)))

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

(def z-state (atom :release))
(def last-z (atom (now)))

(defn z-listener
  []
  (when (= @z-state :release)
      (socket/send! @ws {:type "cat"
                         :key "jump"})
      (reset! z-state :pressed))
  (reset! last-z (now))
  (js/setTimeout (fn []
                   (when (<= (+ @last-z 20) (now))
                     (reset! z-state :release)))
                 30))

(defn ^:export main []
    (reset! ws (socket/socket
                 (str (socket/get-host-url) "echo/")))
    ;; set open websocket handler
    (socket/listen! @ws :open (fn [e]))
    ;; set msg websocket handler
    (socket/listen! @ws :msg (fn [e] (ac-view/call-event! (. e -data))))
    ;; start view
    (ac-view/bootstrap "game" l-listener r-listener z-listener))

(set! (.-onload js/window) main)
