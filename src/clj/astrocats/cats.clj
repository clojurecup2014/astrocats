(ns astrocats.cats
  (:require [astrocats.util :refer [now]]
            [ring-jetty.util.ws :as ws]
            [clojure.data.json :refer [write-str read-str]]))

(def cats (ref {}))

(defprotocol ICat
  (jump [this])
  (left [this])
  (right [this])
  (update [this game-map])
  (-update-hit [this now-time])
  (-update-energy [this now-time])
  (-update-acc [this])
  (-update-acc-zero [this]))

(defrecord Cat [id theta radius x y pre-x pre-y
                life on width height moving
                acc-x acc-y img hit coin energy charge-start
                pre-radius last-hit-time damaged key jump?]
  ICat
  (jump [this]
    (if (> (:energy this) 0)
      (-> this
        (assoc-in [:acc-y] -8)
        (update-in [:y] dec)
        (assoc-in [:jump?] true))))
  (left [this]
    (case (:key this)
      :left (update-in this [:acc-x] #(- % 0.2))
      :right (assoc-in [:moving] :left)))
  (right [this]
    (case (:key this)
      :right (update-in this [:acc-x] #(+ % 0.2))
      :left (assoc-in this [:moving] :right)))
  (-update-hit [this now-time]
    (if (and (> (- now-time (:last-hit-time this)) 2000)
             (:damaged this))
      (assoc-in this [:damaged] false)))
  (-update-energy [this now-time]
    (if (and (< (:energy this) 5)
             (> (- now-time (:charge-start this)) 1000))
      (-> this
          (assoc-in [:charge-start] now-time)
          (update-in [:energy] inc))))
  (-update-acc [this]
    (case (:moving this)
      :left (update-in this [:acc-x] #(- % 0.35))
      :right (update-in this [:acc-x] #(+ % 0.35))))
  (-update-acc-zero [this]
    (if (< (-> this :acc-x Math/abs) 0.35)
      (assoc-in this [:acc-x] 0)))
  (update [this game-map]
    (let [now-time (now)]
      (-> this
        (.-update-hit now-time)
        (.-update-energy now-time)
        (assoc-in [:pre-x] (:x this))
        (assoc-in [:pre-y] (:y this))
        (assoc-in [:pre-radius] (:radius this))
        (assoc-in [:radius] (-> (+
                                  (- (:y this) (:center-y game-map)) 
                                  (- (:x this) (:center-x game-map)))
                                (Math/pow 2)
                                Math/sqrt))
        (assoc-in [:theta] (Math/atan2 (- (:x this) (:center-x game-map))
                                       (- (:center-y game-map) (:y this))))
        (update-in [:acc-x] #(* % 0.9))
        .-update-acc .-update-acc-zero))))

(defn init-cat
  [theta radius acc-x acc-y img game-map 
   pre-x pre-y pre-radius]
  (let [radian (/ (* Math/PI radius) 180)]
    (map->Cat {:id (gensym) :theta theta :radius radius
               :x (+ (. game-map -center-x)
                     (* (Math/sin radian) radius))
               :y (+ (. game-map -center-y)
                     (* (Math/sin radian) radius))
               :pre-x pre-x :pre-y pre-y :moving nil
               :life 3 :on "" :width 32 :height 32
               :acc-x acc-x :acc-y acc-y :img img
               :hit 0 :coin 0 :energy 5 :charge-start 0
               :pre-radius pre-radius :last-hit-time 0
               :damaged false :jump? false :key nil})))

(defn send-cats! []
  (pmap #(->> (assoc-in (nth 1 %) [:type] "cat")
              write-str
              (ws/send! (nth 0 %))) 
        (->> @cats (map vec) vec)))
