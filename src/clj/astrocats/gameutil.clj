(ns astrocats.gameutil
  [astrocats.util :refer [now]])

(defn- get-width-rad [cat]
  (* 180 (/ (cat :width) (* Math/PI (cat :radius))))
  )


(defn- get-closest-block-with-r [cat blocks]
  (let [sorted (sort #(compare (Math/abs (- (cat :radius) (%1 :radius)))
                               (Math/abs (- (cat :radius) (%2 :radius)))) blocks)]
    (first sorted)
    ))


(defn- get-closest-block-with-theta [cat blocks]
  (let [sorted (sort #(compare (Math/abs (- (cat :theta) (* (+ (%1 :start) (%1 :end)) 0.5)))
                               (Math/abs (- (cat :theta) (* (+ (%2 :start) (%2 :end)) 0.5)))) blocks)]
    (first sorted)
    ))


(defn calc-block-collision
  [cat blocks]
  (let [now-width-rad (* 180 (/ (cat :width) (* Math/PI (cat :radius))))
        same-rad-blocks (for [b blocks :when  (if (and (< (b :start) (+ (cat :theta) (* now-width-rad 0.5)))
                                                       (> (b :end) (- (cat :theta) (* now-width-rad 0.5))))
                                                b)] b)
        same-height-blocks (for [b blocks :when  (if (and (< (b :radius) (+ (cat :radius) (* 0.75 (cat :height))))
                                                       (> (b :radius) (+ (cat :radius) (* 0.25 (cat :height)))))
                                                b)] b)
        hitfrom-top (if (> (count same-rad-blocks) 0)
                         (let [closest-block (get-closest-block-with-r cat same-rad-blocks)]
                           (if (and (> (closest-block :radius) (cat :radius))
                                (< (- (closest-block :radius) (cat :radius)) (cat :radius))
                                (< (- (cat :raduis) (cat :pre-radius)) 0))
                             {:id (closest-block :id) :radius (closest-block :radius)}
                             false
                             )
                           )
                         false)
        hitfrom-bottom (if (> (count same-rad-blocks) 0)
                            (let [closest-block (first same-rad-blocks)]
                              (and (< (- (closest-block :radius) (closest-block :height)) (+ (cat :radius) (cat :height)))
                                   (> (closest-block :radius) (+ (cat :radius) (cat :height)))
                                (> (- (cat :raduis) (cat :pre-radius)) 0))                            
                              )
                            false)
        is-hitfrom-side (if (> (count same-height-blocks) 0)
                          (let [closest-block (first same-height-blocks)]
                            (and (< (closest-block :start) (+ (cat :theta) (/ (2.0 now-width-rad))))
                                 (> (closest-block :end) (- (cat :theta) (/ 2.0 now-width-rad))))
                            )
                          false)
        new-acc-x (if is-hitfrom-side
                    (* 0.2 (cat :acc-x))
                    (cat :acc-x)
                    )
        new-acc-y (if hitfrom-bottom
                    (* (cat :acc-y) -0.3)
                    (cat :acc-y)
                    )
        new-cat (if hitfrom-top
                    {:radius (hitfrom-top :radius) :acc-y 0 :on (hitfrom-top :on) :energy 5}
                    {:radius (cat :radius) :acc-y new-acc-y :on (cat :on) :energy (cat :energy)}
                    )
        ]
    (-> cat
        (assoc-in [:acc-x] new-acc-x)
        (assoc-in [:radius] (new-cat :radius))
        (assoc-in [:acc-y] (new-cat :acc-y))
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
         max (- (count cats) 1)
         c coin]
    (if (< max i)
      c
      (recur (+ 1 i) max (second (calc-coin-collision (nth cats i) c)))
  )))

(defn get-collisioned-cats
  [cat coins]
  (loop [i 0
         max (- (count coins) 1)
         c cat]
    (if (< max i)
      c
      (recur (+ 1 i) max (first (calc-coin-collision c (nth coins i))))
  )))


(defn calc-collision
  [cat1 cat2]
  (let [cat1-width-rad (get-width-rad cat1)
        cat2-width-rad (get-width-rad cat2)
        difspeed (- (- (cat1 :radius) (cat2 :pre-radius)) (- (cat2 :radius) (cat2 :pre-radius)))
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
                          (assoc-in [:acc-x] (* -1 (cat1 :acc-x))))
                      (-> cat2
                          (assoc-in [:acc-x] (* -1 (cat2 :acc-x))))]
                   (if
                       (and (> (+ (cat2 :radius) 
                                  (* (cat2 :height) 0.333)) 
                               (+ (cat1 :radius) 
                                  (cat1 :height)))
                            (< (cat2 :radius) (+ (cat1 :radius) (cat1 :height)))
                            (> difspeed 0)
                            (not (cat1 :damaged)))
                     [(-> cat1
                          (assoc-in [:acc-y] (* -0.2 (cat2 :acc-y)))
                          (assoc-in [:life] (- (cat2 :life) 1))
                          (assoc-in [:damaged] true)
                          (assoc-in [:lasthittime] (now))) 
                      (-> cat2 
                          (assoc-in [:radius] (+ (cat2 :radius) (cat2 :height)) )
                          (assoc-in [:acc-y] -9)
                          (assoc-in [:score] (+ (cat1 :score) 50)))]
                     (if 
                         (and (> (+ (cat2 :radius) (cat2 :height)) (cat1 :radius))
                              (< (+ (cat2 :radius) (* (cat2 :height) 0.666)) (cat1 :radius))
                              (< difspeed 0)
                              (not (cat2 :damaged)))
                       [(-> cat1
                            (assoc-in [:radius] (+ (cat2 :radius) (cat2 :height)) )
                            (assoc-in [:acc-y] -9)
                            (assoc-in [:score] (+ (cat1 :score) 50)))
                        (-> cat2 
                            (assoc-in [:acc-y] (* -0.2 (cat2 :acc-y)))
                            (assoc-in [:life] (- (cat2 :life) 1))
                            (assoc-in [:damaged] true)
                            (assoc-in [:lasthittime] (now))) 
                        ]
                       [cat1 cat2])))
                   [cat1 cat2])
        ]
    new-cats
    )
  )


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

(defn get-now-block
  [blocks id]
  (first (filter #(= (% :id) id) blocks)))

(defn calc-on
  [cat blocks maps]
  (let [theta (/ (* (cat :theta) Math/PI) 180.0)
        theta-x (* (cat :acc-x) (Math/cos theta))
        theta-y (* (cat :acc-x) (Math/sin theta))
        on (if (and (> (count (cat :on)) 0)
                    (not= (cat :on) "ground"))
             (let [nowblock (get-now-block blocks (cat :on))]
               (if (or 
                    (> (nowblock :start) (cat :theta))
                    (< (nowblock :end) (cat :theta)))
                 ""
                 (cat :theta)
               ))
             "")
        new-param (if (= on "")
                    {:radius (- (cat :radius) (cat :acc-y)) 
                     :rad-x (* (cat :acc-y) (Math/sin theta) -1)
                     :rad-y (* (cat :acc-y) (Math/cos theta))
                     :acc-y (if (< (cat :acc-y) 0)
                              (+ (cat :acc-y) 0.5)
                              (if (> (cat :acc-y) 3.5)
                                (+ (cat :acc-y) 0.01)
                                (+ (cat :acc-y) 0.25)))}
                    {:radius (cat :radius)
                     :rad-x 0
                     :rad-y 0
                     :acc-y (cat :acc-y)}
                    )
        new-param2 (if (> (maps :ground-y) (new-param :radius))
                     (-> new-param
                         (assoc-in [:acc-y] 0)
                         (assoc-in [:life] 0)
                         (assoc-in [:on] "ground"))
                     new-param
                     )
        tmp-x (+ (cat :x) (new-param2 :rad-x) theta-x)
        tmp-y (+ (cat :y) (new-param2 :rad-y) theta-y)
        tmp-r (Math/sqrt (+ (Math/pow (- tmp-x (maps :center-x)) 2) 
                            (Math/pow (- tmp-y (maps :center-y)) 2)))
        ]
    (-> cat
        (assoc-in [:radius] (new-param2 :radius))
        (assoc-in [:acc-y] (new-param2 :acc-y))
        (assoc-in [:on] (new-param2 :on))
        (assoc-in [:x] (+ (/ (* (new-param2 :radius) 
                              (- tmp-x (maps :center-x)))
                             tmp-r)
                          (maps :center-x)))
        (assoc-in [:y] (+ (/ (* (new-param2 :radius) 
                              (- tmp-y (maps :center-y)))
                             tmp-r)
                          (maps :center-y)))
        )))

(defn calc-block-ground-collision
  [cat blocks maps]
  (-> cat
      (calc-on blocks maps)
      (calc-block-collision blocks)))

(defn calc-collisions
  [cats blocks coins maps]
  (let [cat-blocks (mapv (fn [c] (calc-block-ground-collision c blocks map)) cats)
        result-cats (mapv (fn [c] (get-collisioned-cats c coins)) cat-blocks)
        result-coins (mapv (fn [c] (get-collisioned-coins c cat-blocks)) coins)
        ]
    [(calc-cats-collisions result-cats)
     result-coins]))
