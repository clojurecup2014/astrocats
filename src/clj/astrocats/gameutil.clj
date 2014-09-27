(ns astrocats.gameutil)

(defn calc-block-collision-pos
  [cat blocks map]
  (let [now-width-rad (* 180 (/ (cat :width) (* (Math/PI (cat :radius)))))
        same-rad-blocks (for [b blocks :when  (if (and (< (b :start) (+ (cat :theta) (/ now-width-rad 2)))
                                                       (> (b :end) (- (cat :theta) (/ now-width-rad 2))))
                                                b)] b)
        is-hitfrom-top ]
    ))

