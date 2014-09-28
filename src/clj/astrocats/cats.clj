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
  (-update-acc-zero [this])
  (pack [this]))

(defrecord Cat [id theta radius x y pre-x pre-y
                life on width height moving
                acc-x acc-y img energy charge-start
                pre-radius last-hit-time damaged? jump? score]
  ICat
  (jump [this]
    (if (> (:energy this) 0)
      (-> this
        (assoc-in [:acc-y] -10)
        (update-in [:y] dec)
        (assoc-in [:on] "")
        (update-in [:energy] dec)
        (assoc-in [:charge-start] (now))
        (assoc-in [:jump?] true))
      this))
  (left [this]
    (case (:moving this)
      :left (update-in this [:acc-x] #(- % 1.5))
      :right (assoc-in this [:moving] :left)
      this))
  (right [this]
    (case (:moving this)
      :right (update-in this [:acc-x] #(+ % 1.5))
      :left (assoc-in this [:moving] :right)
      this))
  (-update-hit [this now-time]
    (if (and (> (- now-time (:last-hit-time this)) 2000)
             (:damaged this))
      (assoc-in this [:damaged] false)
      this))
  (-update-energy [this now-time]
    (if (and (< (:energy this) 5)
             (> (- now-time (:charge-start this)) 1000))
      (-> this
          (assoc-in [:charge-start] now-time)
          (update-in [:energy] inc))
      this))
  (-update-acc-zero [this]
    (if (< (-> this :acc-x Math/abs) 0.35)
      (assoc-in this [:acc-x] 0)
      this))
  (update [this game-map]
    (let [now-time (now)]
      (-> this
          (-update-hit now-time)
          (-update-energy now-time)
          (assoc-in [:pre-x] (:x this))
          (assoc-in [:pre-y] (:y this))
          (assoc-in [:pre-radius] (:radius this))
          (assoc-in [:radius] (Math/sqrt
                               (+ (Math/pow (- (:y this) (:center-y game-map)) 2)
                                  (Math/pow (- (:x this) (:center-x game-map)) 2))))
          (assoc-in [:theta] (double
                              (/ (* 180 (Math/atan2 (- (:x this) (:center-x game-map))
                                                    (- (:center-y game-map) (:y this))))
                                 Math/PI)))
          (update-in [:acc-x] #(* % 0.5))
          -update-acc-zero)))
  (pack [this]
    {:id (:id this)
     :me false
     :theta (:theta this)
     :radius (:radius this)
     :vx (:acc-x this) ; TODO rename!
     :vy (:acc-y this) ; TODO rename!
     :moving (:moving this)
     :img (:img this)
     :score (:score this)
     :life (:life this)
     :energy (:energy this)
     :jump (:jump? this)
     :damaged (:damaged? this)}))

(defn init-cat
  [theta radius acc-x acc-y img game-map
   pre-x pre-y pre-radius]
  (let [radian (/ (* Math/PI theta) 180.0)]
    (map->Cat {:id (str (gensym))
               :theta theta
               :radius radius
               :x (double (+ (. game-map -center-x)
                     (* (Math/cos radian) radius)))
               :y (double (+ (. game-map -center-y)
                     (* (Math/sin radian) radius)))
               :pre-x pre-x :pre-y pre-y
               :life 3
               :on ""
               :width 32
               :height 32
               :moving :left
               :acc-x acc-x :acc-y acc-y
               :img img
               :energy 5
               :charge-start 0
               :pre-radius pre-radius
               :last-hit-time 0
               :damaged? false
               :jump? false
               :score 0})))
