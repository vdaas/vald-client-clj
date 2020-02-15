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
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

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
        {:keys [help? json? elapsed-time?]} options
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
                       (read-string))
            f (fn []
                (vald/update client id vector))]
        (if elapsed-time?
          (time (f))
          (f))
        (println "updated.")))))
