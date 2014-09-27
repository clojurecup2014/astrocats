(ns astrocats.echo
  (:require [astrocats.map :as ac-map]
            [ring-jetty.util.ws :as ws]
            [clojure.data.json :refer [write-str read-str]]))

(def all-sessions (ref #{}))

(def default-blocks
  [[10 30 240 10]
   [40 60 170 10]
   [70 90 260 10]
   [100 140 180 10]
   [130 150 260 10]
   [-170 150 170 10]
   [-110 -80 150 10]
   [-140 -130 260 10]
   [-90 -65 250 10]
   [-40 -25 280 10]
   [-35 -5 150 10]])

(def default-map (ac-map/init-map default-blocks))

(defn- on-connect [session]
  ;; send game map
  (->> (assoc-in default-map [:type] "map")
       write-str 
       (ws/send! session))
  (dosync
   (alter all-sessions conj session)))

(defn- on-close [session code reason]
  (dosync
   (alter all-sessions disj session)))

(defn- on-text [session message]
  (doseq [s @all-sessions]
    (ws/send! s (str
                 (.. session getSession getRemoteAddress getHostName)
                 ":"
                 message))))

(defn- on-bytes [session payload offset len]
  nil)

(defn- on-error [session e]
  (.printStackTrace e)
  (dosync
   (alter all-sessions disj session)))

(def handler
  {:on-connect on-connect
   :on-error on-error
   :on-text on-text
   :on-close on-close
   :on-bytes on-bytes})
