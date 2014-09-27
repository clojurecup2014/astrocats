(ns astrocats.model.score
  (:require [clojure.string :as str]
            (korma [core :refer :all]
                   [db :refer :all])))

(declare score)

(defentity score)

(defn now-time []
   (int (/ (.getTime (java.util.Date.)) 1000)))

(defn get-all-ranking
  []
  (select score (where (> :id 0))
                (order :score)))

(defn add-ranking
  [name score]
  (let [add-time (now-time)]
    (insert member
            (values {:name name
                     :score score
                     :updated_at add-time}))))
