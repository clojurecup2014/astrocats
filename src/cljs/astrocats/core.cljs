(ns astrocats.core
  (:require [astrocats.sockets.core :as sockets]
            [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(def game-map (atom nil))

(defn -main [] 
  (let [ws (sockets/socket 
             (str (sockets/get-host-url) "echo/"))]
    ;; set open websocket handler
    (sockets/listen! ws :open (fn [e] (print "connected!")))
    ;; set on message handler
    (sockets/listen! ws :msg #(case (. % -data)
                               :map (->> % .-data 
                                         (.parse js/JSON) 
                                         (js->clj :keywordize-keys true)
                                         (reset! game-map)
                                         print)
                               (-> (sel1 :#message) 
                                   (dommy/append! (node 
                                                    [:p (. % -data)])))))
    ;; set button handler
    (dommy/listen! (sel1 :#send-button)
                   :click (fn [e]
                            (->> (sel1 :#send-text) dommy/value (sockets/send! ws))))))

(set! *print-fn* #(.log js/console (apply str %&)))
(set! (.-onload js/window) -main)
