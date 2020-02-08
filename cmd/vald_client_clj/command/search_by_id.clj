(ns vald-client-clj.command.search-by-id
  (:require
   [clojure.tools.cli :as cli]
   [vald-client-clj.core :as vald]))

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
                :timeout timeout}
        id (first arguments)]
    (if help?
      (println summary)
      (vald/search-by-id client id config))))
