(ns vald-client-clj.util
  (:require
    [clojure.string :as string])
  (:import
    [java.io BufferedReader]))

(defn rand-vec [dim]
  (take dim (repeatedly #(float (rand)))))

(defn rand-vecs [n dim]
  (take n (repeatedly #(rand-vec dim))))

(defn read-from-stdin []
  (->> (BufferedReader. *in*)
       (line-seq)
       (string/join "\n")))
