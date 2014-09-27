(ns astrocats.gameutil)

(defn calc-block-collision
  [cat blocks map]
  (let [now-width-rad (* 180 (/ (cat :width) (* (Math/PI (cat :radius)))))
        same-rad-blocks (for [b blocks :when  (if (> (b :x) 10) b)] b)]
    ))

(defrecord Coin [id radius theta exist])

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
                center-x center-y blocks])

(defn init-map
  ([blocks]
    (init-map 800 600 80 blocks))
  ([width height ground blocks]
    (map->Map {:width width :height height :ground-y ground 
               :center-x (/ width 2) :center-y 450
               :blocks blocks})))
