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
  (-update-acc-zero [this])
  (pack [this]))

(defrecord Cat [id theta radius x y pre-x pre-y
                life on width height moving
                acc-x acc-y
                img
                hit coin energy charge-start
                pre-radius last-hit-time damaged? jump?
                key ; TODO remove?
                score]
  ICat
  (jump [this]
    (if (> (:energy this) 0)
      (-> this
        (assoc-in [:acc-y] -8)
        (update-in [:y] dec)
        (assoc-in [:jump?] true))))
  (left [this]
    (case (:moving this)
      :left (update-in this [:acc-x] #(- % 0.2))
      :right (assoc-in this [:moving] :left)))
  (right [this]
    (case (:moving this)
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
        (assoc-in [:theta] (double
                            (/ (* 180 (Math/atan2 (- (:x this) (:center-x game-map))
                                                  (- (:center-y game-map) (:y this))))
                               Math/PI)))
        (update-in [:acc-x] #(* % 0.9))
        .-update-acc .-update-acc-zero)))
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
  (let [radian (/ (* Math/PI radius) 180)]
    (map->Cat {:id (gensym)
               :theta theta
               :radius radius
               :x (+ (. game-map -center-x)
                     (* (Math/sin radian) radius))
               :y (+ (. game-map -center-y)
                     (* (Math/sin radian) radius))
               :pre-x pre-x :pre-y pre-y
               :life 3
               :on ""
               :width 32
               :height 32
               :moving :left
               :acc-x acc-x :acc-y acc-y
               :img img
               :hit 0
               :coin 0
               :energy 5
               :charge-start 0
               :pre-radius pre-radius
               :last-hit-time 0
               :damaged? false
               :jump? false
               :key nil
               :score 0})))

(defn send-all-cats! []
  (locking @cats
    (let [all-cats @cats]
      (doseq [s (keys all-cats)]
        (let [my-c (all-cats s)]
          (doseq [c (vals all-cats)]
            ;; TODO fix loop freeze bug
            (ws/send! s #(-> c
                             pack
                             (assoc :type "cat"
                                    :me (= my-c c))
                             write-str))))))))
