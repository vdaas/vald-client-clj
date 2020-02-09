(ns vald-client-clj.command.update
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read as json"
    :id :json?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] update [SUBOPTIONS] ID VECTOR"
        ""
        "Update single vector."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json?]} options
        read-string (if json?
                      util/read-json
                      edn/read-string)
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (let [vector (-> (or (second arguments)
                           (util/read-from-stdin))
                       (read-string))]
        (vald/update client id vector)
        (println "updated.")))))
