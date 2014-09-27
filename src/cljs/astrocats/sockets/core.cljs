(ns astrocats.sockets.core)

(defn socket
  "init Websocket object"
  [url]
    (js/WebSocket. url))

(defn get-host-url 
  "provide default host url"
  []
  (str "ws://"
       (.. js/window -location -host)
       (.. js/window -location -pathname)))

(defn send!
  "send clj-map in websocket"
  [ws clj]
  (->> clj clj->js (.stringify js/JSON) (.send ws)))

(defn listen!
  "set websocket listener. (support ev-key is :msg or :open)"
  [ws ev-key f]
  (case ev-key
    :msg (set! (. ws -onmessage) f)
    :open (set! (. ws -onopen) f)))

(defn close!
  "close websocket"
  ([ws]
    (.close ws 1000 "close"))
  ([ws code]
    (.close ws code "close"))
  ([ws code text]
    (.close ws code text)))
