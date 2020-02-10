(ns vald-client-clj.command.exists
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-h" "--help" :id :help?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] exists [SUBOPTIONS] ID"
        ""
        "Check whether ID exists or not."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help?]} options
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (-> client
          (vald/exists id)
          (println)))))
