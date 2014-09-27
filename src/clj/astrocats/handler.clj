(ns astrocats.handler
  (:require [astrocats.map :refer [cats coins blocks default-map]]
            [astrocats.gameutil :refer [collision]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

(defn init []
  (future
    (while true
      (let [res (collision @cats @blocks @coins)
            new-cats (nth 0 res)
            new-blocks (nth 1 res)
            new-coins (nth 2 res)]
        ;; cats
        (dosync 
          (alter cats #(new-cat))
          (alter blocks #(new-blocks))
          (alter coins #(new-coins)))

(defn- page []
  (html5 [:head [:title "astrocats"] 
          (include-js "/js/astrocats.js")]
         [:body
          [:div#form
           [:input#send-text {:type "text"}]
           [:input#send-button {:type "button" :value "Send"}]]
          [:div#message]]))

(defroutes app-routes
  (GET "/" [] (page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
