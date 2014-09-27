(ns astrocats.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

(defn- page []
  (html5 [:head [:title "astrocats"] 
          (include-js "/js/phaser.min.js")
          (include-js "/js/astrocats.js")]
         [:body
          [:div#game]
          ]))

(defroutes app-routes
  (GET "/" [] (page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
