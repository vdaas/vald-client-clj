(ns vald-client-clj.command.index-info
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "write as json"
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] index-info [SUBOPTIONS]"
        ""
        "Fetch index info."
        "This functionality is only for Agents."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? elapsed-time?]} options
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/index-info)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
