(ns vald-client-clj.command.stream-search-by-id
  (:require
   [clojure.tools.cli :as cli]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-n" "--num NUM"
    :id :num
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--radius RADIUS"
    :id :radius
    :default 1.0
    :parse-fn #(Float/parseFloat %)]
   ["-e" "--epsilon EPSILON"
    :id :epsilon
    :default 0.1
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--timeout TIMEOUT"
    :id :timeout
    :default 100000
    :parse-fn #(Integer/parseInt %)]])

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? num radius epsilon timeout]} options
        config {:num num
                :radius radius
                :epsilon epsilon
                :timeout timeout}]
    (if help?
      (println summary)
      (let [ids (-> (or (first arguments)
                        (util/read-from-stdin))
                    (edn/read-string))]
        (-> client
            (vald/stream-search-by-id ids config)
            (println))))))
