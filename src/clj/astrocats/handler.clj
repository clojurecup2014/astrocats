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
  (println "init")
  (future
    (while true
      (println "begin loop")
      (Thread/sleep 1000)
      ;; update cats
      (locking ac-cats/cats
        (sync
          (alter ac-cats/cats
            (fn [x] (zipmap (-> x keys)
                            (->> x vals (map update)))))))
      (println "---")
      (println "cat :" (count (keys @ac-cats/cats)) ":" @ac-cats/cats)
      (println "---")
      (ac-cats/send-all-cats!)
      (println "end loop")
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
