(ns astrocats.handler
  (:require [astrocats.map :as ac-maps]
            [astrocats.cats :as ac-cats]
            [astrocats.util :refer [now]]
            [astrocats.gameutil :as game]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring-jetty.util.ws :as ws]
            [clojure.data.json :refer [write-str read-str]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

(defn- send-all-cats! []
  (dosync
   (let [all-cats @ac-cats/cats]
     (doseq [s (keys all-cats)]
       (when-not (nil? s)
         (let [my-c (get all-cats s)]
           (when-not (nil? my-c)
             (doseq [c (vals all-cats)]
               (try
                 (ws/send! s (-> c
                                 ac-cats/pack
                                 (assoc :type "cat"
                                        :me (= my-c c))
                                 write-str))
                 (catch Exception e (do (.printStackTrace e)
                                        nil)))))))))))

(defn init []
  (println "init")
  (future
    (while true
      ;; (println "---")
      (Thread/sleep 10)
      ;; update cats
      ;; (println (now) "cat :" (count (keys @ac-cats/cats)) ":" (vals @ac-cats/cats))
      (dosync
        (alter ac-cats/cats
          (fn [x]
            (try
              (zipmap (-> x keys)
                      (->> x vals (map #(ac-cats/update % ac-maps/default-map))))
              (catch Exception e (do (.printStackTrace e)
                                     nil))))))
      ;; (println (now) "cat :" (count (keys @ac-cats/cats)) ":" (vals @ac-cats/cats))
      (send-all-cats!)
      (try
        (let [[new-cats new-coins] (game/calc-collisions @ac-cats/cats ac-maps/blocks @ac-maps/coins ac-maps/default-map)]
          ;; update vars
          ;; (println new-cats)
          ;; (println new-coins)
          (dosync
           (ref-set ac-cats/cats new-cats)
           (ref-set ac-maps/coins new-coins)))
        (catch Exception e (do (.printStackTrace e)
                               (System/exit 1)
                               nil)))
      ;; (println (now) "cat :" (count (keys @ac-cats/cats)) ":" (vals @ac-cats/cats))
      )))

(defn- page []
  (html5 [:head [:title "astrocats"]
          (include-css "/css/default.css")
          (include-js "/js/phaser.min.js")
          (include-js "/js/astrocats.js")]
         [:body
          [:div#game]]))

(defroutes app-routes
  (GET "/" [] (page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
