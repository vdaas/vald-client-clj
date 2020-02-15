(ns vald-client-clj.command.get-object
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
        {:keys [help? json? elapsed-time?]} options
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/get-object id)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
