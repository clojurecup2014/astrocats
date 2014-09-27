(ns astrocats.gameutil)

(defn calc-block-collision
  [cat blocks]
  (let [now-width-rad (* 180 (/ (cat :width) (* Math/PI (cat :radius))))
        same-rad-blocks (for [b blocks :when  (if (and (< (b :start) (+ (cat :theta) (/ now-width-rad 2)))
                                                       (> (b :end) (- (cat :theta) (/ now-width-rad 2))))
                                                b)] b)
        same-height-blocks (for [b blocks :when  (if (and (< (b :radius) (+ (cat :radius) (* 0.75 (cat :height))))
                                                       (> (b :radius) (+ (cat :radius) (* 0.25 (cat :height)))))
                                                b)] b)
        hitfrom-top (if (> (count same-rad-blocks) 0)
                         (let [closest-block (first same-rad-blocks)]
                           (if (and (> (closest-block :radius) (cat :radius))
                                (< (- (closest-block :radius) (cat :radius)) (cat :radius))
                                (< (- (cat :raduis) (cat :preradius)) 0))
                             {:id (closest-block :id) :radius (closest-block :radius)}
                             false
                             )
                           )
                         false)
        hitfrom-bottom (if (> (count same-rad-blocks) 0)
                            (let [closest-block (first same-rad-blocks)]
                              (and (< (- (closest-block :radius) (closest-block :height)) (+ (cat :radius) (cat :height)))
                                   (> (closest-block :radius) (+ (cat :radius) (cat :height)))
                                (> (- (cat :raduis) (cat :preradius)) 0))                            
                              )
                            false)
        is-hitfrom-side (if (> (count same-height-blocks) 0)
                          (let [closest-block (first same-height-blocks)]
                            (and (< (closest-block :start) (+ (cat :theta) (/ (2 now-width-rad))))
                                 (> (closest-block :end) (- (cat :theta) (/ 2 now-width-rad))))
                            )
                          false)
        new-acc-x (if is-hitfrom-side
                    (* 0.2 (cat :acc_x))
                    (cat :acc_x)
                    )
        new-acc-y (if hitfrom-bottom
                    (* (cat :acc_y) -0.3)
                    (cat :acc_y)
                    )
        new-cat (if hitfrom-top
                    {:radius (hitfrom-top :radius) :acc_y 0 :on (hitfrom-top :on) :energy 5}
                    {:radius (cat :radius) :acc_y new-acc-y :on (cat :on) :energy (cat :energy)}
                    )
        ]
    (-> cat
        (assoc-in [:acc_x] new-acc-x)
        (assoc-in [:radius] (new-cat :radius))
        (assoc-in [:acc_y] (new-cat :acc_y))
        (assoc-in [:on] (new-cat :on))
        (assoc-in [:energy] (new-cat :energy)))
    ))


(defn calc-coin-collision
  [cat coin]
  (let [now-width-rad (* 180 (/ (cat :width) (* Math/PI (cat :radius))))
        new-param (if (coin :exist)
                   (if (and (> (coin :theta) (- (cat :theta) (/ now-width-rad 2)))
                            (< (coin :theta) (+ (cat :theta) (/ now-width-rad 2)))
                            (> (coin :radius) (cat :radius))
                            (< (coin :radius) (+ (cat :radius) (cat :height))))
                     {:score (+ (cat :score) 10) :exist false}
                     {:score (cat :score) :exist (coin :exist)})
                   {:score (cat :score) :exist (coin :exist)}
                   )]
    [
     (-> cat
         (assoc-in [:score] (new-param :score)))
     (-> coin
         (assoc-in [:exist] (new-param :exist)))]
  ))

(defn calc-cats-collision
  [cats]
  )

(defn calc--collision
  [cat1 cat2]
  (let [now-width-rad (* 180 (/ (cat :width) (* Math/PI (cat :radius))))]
    )
  )

