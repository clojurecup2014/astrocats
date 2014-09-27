(ns astrocats.handler
  (:require [astrocats.map :as ac-maps]
            [astrocats.cats :as ac-cats]
            [astrocats.macros :refer [locksync]]
            ;;[astrocats.gameutil :refer [collision]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

(defn init []
  (future
    (while true
      (Thread/sleep 1000)
      ;; update cats
      (locksync ac-cats/cats 
        (alter ac-cats/cats 
          (fn [x] (zipmap (-> x keys reverse)
                          (->> x vals (map #(ac-cats/update % ac-maps/default-map)))))))
      (println "cat: " @ac-cats/cats)
      (ac-cats/send-cats!)
      (comment
        (let [res (collision @ac-cats/cats @ac-maps/blocks @ac-maps/coins)
              new-cats (nth 0 res)
              new-coins (nth 1 res)]
          ;; update vars
          (dosync 
            (alter ac-cats/cats #(new-cat))
            (alter ac-maps/coins #(new-coins))))))))

(defn- page []
  (html5 [:head [:title "astrocats"] 
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
