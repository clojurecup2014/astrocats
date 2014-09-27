(defproject astrocats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.9"]
                 [org.clojure/data.json "0.2.5"]
                 [hiccup "1.0.5"]
                 [korma "0.3.0-RC5"]
                 [ring-jetty/ring-ws "0.1.0-SNAPSHOT"]
                 [prismatic/dommy "0.1.3"]
                 [org.clojure/clojurescript "0.0-2342"]]
  :plugins [[lein-ring-jetty "0.1.0-SNAPSHOT"]
            [lein-cljsbuild "1.0.3"]]
  :source-paths ["src/clj"]
  :ring {:init astrocats.handler/init
         :handler astrocats.handler/app
         :websockets {"/echo" astrocats.echo/handler}}
  :profiles
  {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                        [ring-mock "0.1.5"]]}}
  :cljsbuild {:builds ;;{:dev
                      ;; {:id "dev"
                      ;;  :source-paths ["src/cljs"]
                      ;;  :jar true
                      ;;  :compiler {:externs ["externs/astrocats_externs.js"]
                      ;;             :pretty-print true
                      ;;             :optimizations :none
                      ;;             :output-to "public/astrocats_dev.js"
                      ;;             :source-map "public/astrocats_dev.js.map"
                      ;;             :output-dir "public/dev"}}
                      {:release
                       {:id "release"
                        :source-paths ["src/cljs"]
                        :jar true
                        :compiler {:externs ["externs/astrocats_externs.js"]
                                   :pretty-print true
                                   :optimizations :whitespace
                                   :output-to "resources/public/js/astrocats.js"}}}})
