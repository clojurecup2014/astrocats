(ns astrocats.map
  (:require [clojure.data.json :refer [write-str read-str]]
            [ring-jetty.util.ws :as ws]))

(defrecord Coin [id radius theta exist])

(def coins (ref #{}))

(defn init-coin
  [theta radius]
  (map->Coin {:id (gensym) :radius radius
              :theta theta :exist true}))

(defrecord Block [id radius start end height])

(defn init-block
  [start end radius height]
  (map->Block {:id (gensym) :radius radius
               :start start :end end
               :height height}))

(defrecord Map [width height ground-y 
                center-x center-y])

(defn init-map
  ([]
    (init-map 800 600 80))
  ([width height ground]
    (map->Map {:width width :height height :ground-y ground 
               :center-x (/ width 2) :center-y 450})))

(def default-blocks
  [[10 30 240 10]
   [40 60 170 10]
   [70 90 260 10]
   [100 140 180 10]
   [130 150 260 10]
   [-170 150 170 10]
   [-110 -80 150 10]
   [-140 -130 260 10]
   [-90 -65 250 10]
   [-40 -25 280 10]
   [-35 -5 150 10]])

;; game blocks
(def blocks (->> default-blocks (map #(apply init-block %))))
;; game map
(def default-map (init-map))

(comment
(defn send-coins! []
  (pmap #(->> {:type "coins" :coins @coins} 
              write-str 
              (ws/send! %)) (->> @cats (map key) vec)))
  )
