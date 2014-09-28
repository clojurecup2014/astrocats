(ns astrocats.echo
  (:require [astrocats.map :as ac-maps]
            [astrocats.cats :as ac-cats]
            [ring-jetty.util.ws :as ws]
            [clojure.data.json :refer [write-str read-str]]))

(def all-sessions (ref #{}))
(def default-imgs
  (->> (range 0 10) (map #(str "cat" %))))

(def max-session 10)

(defn rand-img
  [imgs]
  (let [new-img (rand-nth default-imgs)]
    (if (seq imgs)
      (if (imgs new-img)
        (rand-img imgs)
        new-img)
      new-img)))

(defn- on-connect [session]
  (if (< (-> @ac-cats/cats vals count) max-session)
    (let [imgs (if (seq @ac-cats/cats)
                 (->> @ac-cats/cats vals (map :img) set))
          b (rand-nth ac-maps/blocks)
          theta (/ (+ (:start b) (:end b)) 2)
          radius (+ (:radius b) 26)
          new-cat (ac-cats/init-cat theta
                                    radius
                                    0 0
                                    (rand-img imgs)
                                    ac-maps/default-map)]
      ;; send cat
      (ws/send! session
                (-> new-cat
                    ac-cats/pack
                    (assoc :type "cat" :me true)
                    write-str))
      ;; add new-cat
      (dosync
        (alter ac-cats/cats assoc session new-cat))
      ;; send blocks
      (->> {:type "blocks" :blocks ac-maps/blocks}
           write-str
           (ws/send! session))
      ;; send game map
      (->> (assoc-in ac-maps/default-map [:type] "map")
           write-str
           (ws/send! session))
      (dosync
        (alter all-sessions conj session)))
    ;; full of max sessions
    (do
      (ws/send! session "full!!")
      (ws/close! session))))

(defn- on-close [session code reason]
  (dosync
   (alter all-sessions disj session))
  (dosync
   (alter ac-cats/cats dissoc session)))

(defn- on-text [session message]
  (let [dt (-> message (read-str :key-fn keyword))]
    (case (:type dt)
      "cat" (dosync
              (alter ac-cats/cats #(case (:key dt)
                                    "left" (update-in % [session] ac-cats/left)
                                    "right" (update-in % [session] ac-cats/right)
                                    "jump" (update-in % [session] ac-cats/jump)))))))

(defn- on-bytes [session payload offset len]
  nil)

(defn- on-error [session e]
  (println (->> (get @ac-cats/cats) :img " has unexceptedly closed."))
  (dosync
   (alter all-sessions disj session))
  (dosync
   (alter ac-cats/cats dissoc session)))

(def handler
  {:on-connect on-connect
   :on-error on-error
   :on-text on-text
   :on-close on-close
   :on-bytes on-bytes})
