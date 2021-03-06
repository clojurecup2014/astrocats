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
                   (when (<= (+ @last-z 4) (now))
                     (reset! z-state :release)))
                 5))

(defn listen-start! []
    (reset! ws (socket/socket
                 (str (socket/get-host-url) "echo/")))
    ;; set open websocket handler
    (socket/listen! @ws :open (fn [e]))
    ;; set msg websocket handler
    (socket/listen! @ws :msg (fn [e] (ac-view/call-event! (. e -data))))
    ;; set error websocket handler
    (socket/listen! @ws :error (fn [e] (js/alert (. e -data)))))

(defn ^:export main []
    ;; start view
    (ac-view/bootstrap "game" l-listener r-listener z-listener listen-start!))

(set! (.-onload js/window) main)
