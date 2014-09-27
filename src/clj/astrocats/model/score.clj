(ns astrocats.model.score
  (:require [clojure.string :as str]
            (korma [core :refer :all]
                   [db :refer :all])))

(declare score)

(def rankin 100)

(defentity score)

(defn now-time []
   (int (/ (.getTime (java.util.Date.)) 1000)))

(defn get-all-ranking
  []
  (select score (where (> :id 0))
                (order :score)))

(defn rankin?
  [score]
  (let [ranking (get-all-ranking)
        rank (mapv (fn [t] (if (> score (t :score)) 1 0)) ranking)]
    (.indexOf rank 1)
    ))

(defn add-ranking
  [name score cattype]
  (let [add-time (now-time)]
    (insert member
            (values {:name name
                     :score score
                     :cattype cattype
                     :updated_at add-time}))))
