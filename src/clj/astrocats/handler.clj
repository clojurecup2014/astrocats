(ns astrocats.handler
  (:require [astrocats.map :refer [coins blocks default-map]]
            [astrocats.cats :refer [cats init-cat send-cats!]]
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
      (dosync
        (alter cats #(pmap (fn [cat] (.update cat)) %)))
      (send-cats!)
      (comment
        (let [res (collision @cats @blocks @coins)
              new-cats (nth 0 res)
              new-blocks (nth 1 res)
              new-coins (nth 2 res)]
          ;; update vars
          (dosync 
            (alter cats #(new-cat))
            (alter blocks #(new-blocks))
            (alter coins #(new-coins))))))))

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
