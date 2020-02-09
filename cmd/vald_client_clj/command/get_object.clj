(ns vald-client-clj.command.get-object
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-h" "--help" :id :help?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] get-object [SUBOPTIONS] ID"
        ""
        "Get object info of single ID."
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
          (vald/get-object id)
          (println)))))
