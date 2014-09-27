(ns astrocats.macros)

(defmacro locksync
  [o & exprs]
  `(locking ~o
     (sync nil ~@exprs)))
