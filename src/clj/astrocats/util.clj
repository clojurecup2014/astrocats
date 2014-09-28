(ns astrocats.util)

(defn now []
  (-> (java.util.Date.) .getTime))
