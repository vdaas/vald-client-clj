(ns vald-client-clj.util
  (:require
   [clojure.string :as string]
   [clojure.pprint :as pprint]
   [camel-snake-kebab.core :as csk]
   [jsonista.core :as json])
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

(def mapper
  (json/object-mapper
    {:encode-key-fn name
     :decode-key-fn csk/->kebab-case-keyword}))

(defn read-json [s]
  (json/read-value s mapper))

(defn ->json [obj]
  (json/write-value-as-string obj mapper))

(defn ->edn [obj]
  (with-out-str (pprint/pprint obj)))

(defn ->minified-edn [obj]
  (with-out-str (pr obj)))
