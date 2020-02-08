(ns vald-client-clj.substitute.reflector
  (:import [com.oracle.svm.core.annotate Substitute TargetClass]))

(deftype ^{TargetClass {:targetClass "clojure.lang.Reflector"}
           Substitute true}
  (^{Substitute true}
    canAccess [this m target]
    true))
