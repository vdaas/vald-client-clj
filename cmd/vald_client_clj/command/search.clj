(ns vald-client-clj.command.search
  (:require
   [clojure.tools.cli :as cli]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-n" "--num"
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--radius"
    :default 0
    :parse-fn #(Float/parseFloat %)]
   ["-e" "--epsilon"
    :default 0
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--timeout"
    :default 0
    :parse-fn #(Integer/parseInt %)]])

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? num radius epsilon timeout]} options
        vector (-> (or (first arguments)
                       (util/read-from-stdin))
                   (edn/read-string))
        config {:num num
                :radius radius
                :epsilon epsilon
                :timeout timeout}]
    (if help?
      (println summary)
      (vald/search client vector config))))
