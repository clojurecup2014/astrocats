(defproject astrocats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.9"]
                 [hiccup "1.0.5"]
                 [ring-jetty/ring-ws "0.1.0-SNAPSHOT"]]
  :plugins [[lein-ring-jetty "0.1.0-SNAPSHOT"]]
  :ring {:handler astrocats.handler/app
         :websockets {"/echo" astrocats.echo/handler}}
  :profiles
  {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                        [ring-mock "0.1.5"]]}})
