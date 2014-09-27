(ns astrocats.gameutil)

(defn- get-width-rad [cat]
  (* 180 (/ (cat :width) (* Math/PI (cat :radius))))
  )

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

(defn get-collisioned-coins
  [coin cats]
  (loop [i 0
         max (count cats)
         c coin]
    (if (< max i)
      c
      (recur (+ 1 i) max (second (calc-coin-collision (nth cats i) c)))
  )))

(defn get-collisioned-cats
  [cat coins]
  (loop [i 0
         max (count coins)
         c cat]
    (if (< max i)
      c
      (recur (+ 1 i) max (first (calc-coin-collision c (nth coins i))))
  )))

(defn calc-cats-collisions
  [cats]
  (mapv
   (fn [c]
     (let [targets (filter #(not= % c) cats)]
       (loop [rtargets targets]
         (if (empty? rtargets)
           c
           (let [t (first targets)
                 [nc nt] (calc-collision c t)]
             (if (not= c nc) 
               nc                      ;; when collision
               (recur (rest rtargets)) ;; when NOT collision
               ))))))
   cats)
  )


(defn calc-collision
  [cat1 cat2]
  (let [cat1-width-rad (get-width-rad cat1)
        cat2-width-rad (get-width-rad cat2)
        difspeed (- (- (cat1 :radius) (cat2 :preradius)) (- (cat2 :radius) (cat2 :preradius)))
        is-rad-col (and (< (- (cat2 :theta) (/ cat2-width-rad 2)) 
                           (+ (cat1 :theta) (/ cat1-width-rad 2)))
                        (> (+ (cat2 :theta) (/ cat2-width-rad 2)) 
                           (- (cat1 :theta) (/ cat1-width-rad 2))))
                     
        new-cats (if is-rad-col
                   (if
                       (and  (> (+ (cat2 :radius) (cat2 :height)) (cat1 :radius))
                             (< (cat2 :radius) (+ (cat1 :radius) (cat1 :height)))
                             (> 0.5 difspeed -0.5))
                     [(-> cat1 
                          (assoc-in [:acc_x] (* -1 (cat1 :acc_x))))
                      (-> cat2
                          (assoc-in [:acc_x] (* -1 (cat2 :acc_x))))]
                   (if
                       (and (> (+ (cat2 :radius) 
                                  (* (cat2 :height) 0.333)) 
                               (+ (cat1 :radius) 
                                  (cat1 :height)))
                            (< (cat2 :radius) (+ (cat1 :radius) (cat1 :height)))
                            (> difspeed 0)
                            (not (cat1 :damaged)))
                     [(-> cat1
                          (assoc-in [:acc_y] (* -0.2 (cat2 :acc_y)))
                          (assoc-in [:life] (- (cat2 :life) 1))
                          (assoc-in [:damaged] true)
                          (assoc-in [:lasthittime] 0)) ;;TODO
                      (-> cat2 
                          (assoc-in [:radius] (+ (cat2 :radius) (cat2 :height)) )
                          (assoc-in [:acc_y] -9)
                          (assoc-in [:score] (+ (cat1 :score) 50)))]
                     (if 
                         (and (> (+ (cat2 :radius) (cat2 :height)) (cat1 :radius))
                              (< (+ (cat2 :radius) (* (cat2 :height) 0.666)) (cat1 :radius))
                              (< difspeed 0)
                              (not (cat2 :damaged)))
                       [(-> cat1
                            (assoc-in [:radius] (+ (cat2 :radius) (cat2 :height)) )
                            (assoc-in [:acc_y] -9)
                            (assoc-in [:score] (+ (cat1 :score) 50)))
                        (-> cat2 
                            (assoc-in [:acc_y] (* -0.2 (cat2 :acc_y)))
                            (assoc-in [:life] (- (cat2 :life) 1))
                            (assoc-in [:damaged] true)
                            (assoc-in [:lasthittime] 0)) ;;TODO
                        ]
                       [cat1 cat2])))
                   [cat1 cat2])
        ]
    new-cats
    )
  )

(defn calc-collisions
  [cats blocks coins]
  (let [cat-blocks (mapv (fn [c] (calc-block-collision c blocks)) cats)
        result-cats (mapv (fn [c] (get-collisioned-cats c coins)) cat-blocks)
        result-coins (mapv (fn [c] (get-collisioned-coins coin cat-blocks)) coins)
        ]
    [(calc-cats-collisions result-cats)
     result-coins]))
