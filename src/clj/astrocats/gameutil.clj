(ns astrocats.gameutil
  (:require [astrocats.util :refer [now]]))

(defn- get-width-rad [cat]
  (* 180 (/ (:width cat) (* Math/PI (:radius cat))))
  )


(defn- get-closest-block-with-r [cat blocks]
  (let [sorted (sort #(compare (Math/abs (- (:radius cat) (:radius %1)))
                               (Math/abs (- (:radius cat) (:radius %2)))) blocks)]
    (first sorted)))


(defn- get-closest-block-with-theta [cat blocks]
  (let [sorted (sort #(compare (Math/abs (- (:theta cat) (* (+ (:start %1) (:end %1)) 0.5)))
                               (Math/abs (- (:theta cat) (* (+ (:start %2) (:end %2)) 0.5)))) blocks)]
    (first sorted)))

(defn calc-block-collision
  [cat blocks]
  (let [now-width-rad (* 180 (/ (:width cat) (* Math/PI (:radius cat))))
        same-rad-blocks (for [b blocks :when (if (and (< (:start b) (+ (:theta cat) (* now-width-rad 0.5)))
                                                      (> (:end b) (- (:theta cat) (* now-width-rad 0.5))))
                                               b)] b)
        same-height-blocks (for [b blocks :when (if (and (< (:radius b) (+ (:radius cat) (* 0.75 (:height cat))))
                                                         (> (:radius b) (+ (:radius cat) (* 0.25 (:height cat)))))
                                                  b)] b)
        hitfrom-top (if (> (count same-rad-blocks) 0)
                         (let [closest-block (get-closest-block-with-r cat same-rad-blocks)]
                           (if (and (> (:radius closest-block) (:radius cat))
                                (< (- (:radius closest-block) (:radius cat)) (:radius cat))
                                (< (- (:radius cat) (:pre-radius cat)) 0))
                             {:id (:id closest-block) :radius (:radius closest-block)}
                             false))
                         false)
        hitfrom-bottom (if (> (count same-rad-blocks) 0)
                            (let [closest-block (get-closest-block-with-r cat same-rad-blocks)]
                              (and (< (- (:radius closest-block) (:height closest-block)) (+ (:radius cat) (:height cat)))
                                   (> (:radius closest-block) (+ (:radius cat) (:height cat)))
                                   (> (- (:radius cat) (:pre-radius cat)) 0))
                              )
                            false)
        is-hitfrom-side (if (> (count same-height-blocks) 0)
                          (let [closest-block (get-closest-block-with-theta cat same-height-blocks)]
                            (and (< (:start closest-block) (+ (:theta cat) (/ 2.0 now-width-rad)))
                                 (> (:end closest-block) (- (:theta cat) (/ 2.0 now-width-rad)))))
                          false)
        new-acc-x (if is-hitfrom-side
                    (* -0.2 (:acc-x cat))
                    (:acc-x cat))
        new-acc-y (if hitfrom-bottom
                    (* (:acc-y cat) -0.3)
                    (:acc-y cat))
        new-cat (if hitfrom-top
                    {:radius (:radius hitfrom-top) :acc-y 0 :on (:id hitfrom-top) :energy 5}
                    {:radius (:radius cat) :acc-y new-acc-y :on (:on cat) :energy (:energy cat)})]
    (-> cat
        (assoc-in [:acc-x] new-acc-x)
        (assoc-in [:radius] (:radius new-cat))
        (assoc-in [:acc-y] (:acc-y new-cat))
        (assoc-in [:on] (:on new-cat))
        (assoc-in [:energy] (:energy new-cat)))))

(defn calc-coin-collision
  [cat coin]
  (let [now-width-rad (* 180 (/ (:width cat) (* Math/PI (:radius cat))))
        new-param (if (:exist coin)
                   (if (and (> (:theta coin) (- (:theta cat) (/ now-width-rad 2)))
                            (< (:theta coin) (+ (:theta cat) (/ now-width-rad 2)))
                            (> (:radius coin) (:radius cat))
                            (< (:radius coin) (+ (:radius cat) (:height cat))))
                     {:score (+ (:score cat) 10) :exist false}
                     {:score (:score cat) :exist (:exist coin)})
                   {:score (:score cat) :exist (:exist coin)})]
    [(-> cat
         (assoc-in [:score] (new-param :score)))
     (-> coin
         (assoc-in [:exist] (new-param :exist)))]))

(defn get-collisioned-coins
  [coin cats]
  (loop [i 0
         max (- (count cats) 1)
         c coin]
    (if (< max i)
      c
      (recur (+ 1 i) max (second (calc-coin-collision (nth cats i) c))))))

(defn get-collisioned-cats
  [cat coins]
  (loop [i 0
         max (- (count coins) 1)
         c cat]
    (if (< max i)
      c
      (recur (+ 1 i) max (first (calc-coin-collision c (nth coins i)))))))

(defn calc-collision
  [cat1 cat2]
  (let [cat1-width-rad (get-width-rad cat1)
        cat2-width-rad (get-width-rad cat2)
        difspeed (- (- (:radius cat1) (:pre-radius cat2)) (- (:radius cat2) (:pre-radius cat2)))
        is-rad-col (and (< (- (:theta cat2) (/ cat2-width-rad 2))
                           (+ (:theta cat1) (/ cat1-width-rad 2)))
                        (> (+ (:theta cat2) (/ cat2-width-rad 2))
                           (- (:theta cat1) (/ cat1-width-rad 2))))
        new-cats (if is-rad-col
                   (if (and  (> (+ (:radius cat2) (:height cat2)) (:radius cat1))
                             (< (:radius cat2) (+ (:radius cat1) (:height cat1)))
                             (> 0.5 difspeed -0.5))
                     [(-> cat1
                          (assoc-in [:acc-x] (* -1 (:acc-x cat1))))
                      (-> cat2
                          (assoc-in [:acc-x] (* -1 (:acc-x cat2))))]
                   (if (and (> (+ (:radius cat2)
                                  (* (:height cat2) 0.333))
                               (+ (:radius cat1)
                                  (:height cat1)))
                            (< (:radius cat2) (+ (:radius cat1) (:height cat1)))
                            (> difspeed 0)
                            (not (:damaged cat1)))
                     [(-> cat1
                          (assoc-in [:acc-y] (* -0.2 (:acc-y cat2)))
                          (assoc-in [:life] (- (:life cat2) 1))
                          (assoc-in [:damaged] true)
                          (assoc-in [:last-hit-time] (now)))
                      (-> cat2
                          (assoc-in [:radius] (+ (:radius cat2) (:height cat2)) )
                          (assoc-in [:acc-y] -9)
                          (assoc-in [:score] (+ (:score cat1) 50)))]
                     (if
                         (and (> (+ (:radius cat2) (:height cat2)) (:radius cat1))
                              (< (+ (:radius cat2) (* (:height cat2) 0.666)) (:radius cat1))
                              (< difspeed 0)
                              (not (:damaged cat2)))
                       [(-> cat1
                            (assoc-in [:radius] (+ (:radius cat2) (:height cat2)) )
                            (assoc-in [:acc-y] -9)
                            (assoc-in [:score] (+ (:score cat1) 50)))
                        (-> cat2
                            (assoc-in [:acc-y] (* -0.2 (:acc-y cat2)))
                            (assoc-in [:life] (- (:life cat2) 1))
                            (assoc-in [:damaged] true)
                            (assoc-in [:last-hit-time] (now)))]
                       [cat1 cat2])))
                   [cat1 cat2])]
    new-cats))

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
   cats))

(defn get-now-block
  [blocks id]
  (first (filter #(= (:id %) id) blocks)))

(defn calc-on
  [cat blocks maps]
  (println "calc-on: " cat)
  (let [theta (/ (* (:theta cat) Math/PI) 180.0)
        theta-x (* (:acc-x cat) (Math/cos theta))
        theta-y (* (:acc-x cat) (Math/sin theta))
        on (if (and (> (count (:on cat)) 0)
                    (not= (:on cat) "ground"))
             (let [nowblock (get-now-block blocks (:on cat))]
               (if (or
                    (> (:start nowblock) (:theta cat))
                    (< (:end nowblock) (:theta cat)))
                 ""
                 (:theta cat)))
             "")
        new-param (if (= on "")
                    {:radius (- (:radius cat) (:acc-y cat))
                     :rad-x (* (:acc-y cat) (Math/sin theta) -1)
                     :rad-y (* (:acc-y cat) (Math/cos theta))
                     :acc-y (if (< (:acc-y cat) 0)
                              (+ (:acc-y cat) 0.5)
                              (if (> (:acc-y cat) 3.5)
                                (+ (:acc-y cat) 0.01)
                                (+ (:acc-y cat) 0.25)))}
                    {:radius (:radius cat)
                     :rad-x 0
                     :rad-y 0
                     :acc-y (:acc-y cat)})
        new-param2 (if (> (:ground-y maps) (:radius new-param))
                     (-> new-param
                         (assoc-in [:acc-y] 0)
                         (assoc-in [:life] 0)
                         (assoc-in [:on] "ground"))
                     new-param)
        tmp-x (+ (:x cat) (:rad-x new-param2) theta-x)
        tmp-y (+ (:y cat) (:rad-y new-param2) theta-y)
        tmp-r (Math/sqrt (+ (Math/pow (- tmp-x (:center-x maps)) 2)
                            (Math/pow (- tmp-y (:center-y maps)) 2)))]
    (-> cat
        (assoc-in [:radius] (:radius new-param2))
        (assoc-in [:acc-y] (:acc-y new-param2))
        (assoc-in [:on] (:on new-param2))
        (assoc-in [:x] (+ (/ (* (:radius new-param2)
                              (- tmp-x (:center-x maps)))
                             tmp-r)
                          (:center-x maps)))
        (assoc-in [:y] (+ (/ (* (:radius new-param2)
                              (- tmp-y (:center-y maps)))
                             tmp-r)
                          (:center-y maps))))))

(defn calc-block-ground-collision
  [cat blocks maps]
  (-> cat
      (calc-on blocks maps)
      (calc-block-collision blocks)))

(defn calc-collisions
 [cats blocks coins maps]
 (let [cat-blocks (mapv #(calc-block-ground-collision % blocks maps) (vals cats))
       result-cats (->> cat-blocks
                        (mapv #(get-collisioned-cats % coins))
                        calc-cats-collisions)
       result-coins (mapv (fn [c] (get-collisioned-coins c cat-blocks)) coins)]
    [(zipmap (keys cats) result-cats)
     result-coins]))
