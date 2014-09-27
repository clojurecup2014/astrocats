(ns astrocats.model.core
  (:require [clojure.string :as str]
            (korma [core :refer :all]
                   [db :refer :all])))

(def ^:private db-test-connection
  {:test-connection-query "SELECT 1"
   :test-connection-on-checkin true
   :idle-connection-test-period 300})

(def ^:private default-config-filename "db.config.clj")

(declare db)

(defn db-init
  ([]
     (db-init default-config-filename))
  ([f]
     (let [rsrc (io/resource f)
           conf (if (nil? rsrc)
                  (throw (RuntimeException. (str "Configuration file not found: " f)))
                  (read-string (slurp rsrc)))]
       (defdb db
         (mysql (merge (:db conf)
                       {:db (:astrocats-db conf)}
                       db-test-connection)))
       nil)))
