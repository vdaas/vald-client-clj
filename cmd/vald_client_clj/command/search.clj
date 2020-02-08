(ns vald-client-clj.command.search
  (:require
   [clojure.tools.cli :as cli]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-n" "--num"
    :id :num
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--radius"
    :id :radius
    :default 0
    :parse-fn #(Float/parseFloat %)]
   ["-e" "--epsilon"
    :id :epsilon
    :default 0
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--timeout"
    :id :timeout
    :default 0
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
      (let [vector (-> (or (first arguments)
                           (util/read-from-stdin))
                       (edn/read-string))]
        (vald/search client vector config)))))
