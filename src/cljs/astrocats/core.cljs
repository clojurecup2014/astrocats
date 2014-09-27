(ns astrocats.core)

(defn -main [] (print "hi"))

(set! *print-fn* #(.log js/console (apply str %&)))
(set! (.-onload js/window) -main)
