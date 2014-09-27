(ns astrocats.gameutil)

(defn calc-block-collision-pos
  [cat blocks map]
  (let [now-width-rad (* 180 (/ (cat :width) (* (Math/PI (cat :radius)))))
        same-rad-blocks (for [b blocks :when  (if (and (< (b :start) (+ (cat :theta) (/ now-width-rad 2)))
                                                       (> (b :end) (- (cat :theta) (/ now-width-rad 2))))
                                                b)] b)
        is-hitfrom-top ]
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
