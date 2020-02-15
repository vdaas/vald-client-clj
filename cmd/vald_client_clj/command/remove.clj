(ns vald-client-clj.command.remove
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-h" "--help" :id :help?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] remove [SUBOPTIONS] ID"
        ""
        "Remove single ID."
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
                (vald/remove-id client id))]
        (if elapsed-time?
          (time (f))
          (f))
        (println "removed.")))))
