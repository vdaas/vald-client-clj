(ns vald-client-clj.command.exists
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-h" "--help" :id :help?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

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
        {:keys [help? elapsed-time?]} options
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/exists id)
                    (println)))]
        (if elapsed-time?
          (time (f))
          (f))))))
